package io.github.mingyifei.datsource.jdbc;

import io.github.mingyifei.datsource.Record;
import io.github.mingyifei.datsource.Source;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * @Description 读角色
 * @Author ming.yifei
 * @Date 2021/12/17 1:44 下午
 **/
@Slf4j
public abstract class JdbcAbstractSource<T> implements Source<T> {
    private JdbcConfig jdbcConfig;
    @Getter
    private Connection queryConnection;
    private Connection deleteConnection;
    protected String key;

    private JdbcOperate.TableInfo tableInfo;
    protected Statement statement;
    protected Statement deleteStatement;

    protected JdbcOperate.TableDefinition tableDefinition;

    private String querySQL;
    private Object lastIndex = 0;

    private LinkedBlockingQueue<Record<T>> queue;
    protected Thread thread = null;
    private volatile boolean running;
    private volatile boolean wait = true;
    private long startTime;
    private long count;
    private final Object lock = new Object();
    protected final Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("[{}] 线程执行异常", t.getName(), e);
        }
    };
    private String endSql;
    private String sql;

    public String getCreateTableSql(String tableName) throws Exception {
        // 建表SQL
        Statement statement = queryConnection.createStatement();
        statement.execute(JdbcOperate.buildQueryCreateSql(tableName));
        ResultSet resultSet = statement.getResultSet();
        if (resultSet.next()) {
            return resultSet.getString("Create Table");
        }
        return null;
    }

    public int getConditionCount() {
        try {
            Statement statement = queryConnection.createStatement();
            String replace = sql.toLowerCase(Locale.ROOT)
                    .replaceAll(" +", " ")
                    .replace("select *", "select count(*)");
            statement.execute(replace);
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public JdbcConfig open(Map<String, Object> config) throws Exception {
        this.jdbcConfig = JdbcConfig.load(config);

        String jdbcUrl = jdbcConfig.getJdbcUrl();
        Assert.notNull(jdbcUrl, "source jdbc url未设置");

        // 加载jdbc
        Class.forName(JdbcOperate.getDriverClassName(jdbcConfig.getJdbcUrl()));
        this.queryConnection = getConnection(jdbcUrl);
        this.deleteConnection = getConnection(jdbcUrl);
        this.queryConnection.setAutoCommit(false);
        this.deleteConnection.setAutoCommit(false);

        // 列信息查询条件
        Assert.notNull(jdbcConfig.getTableName(), "source 表名未设置");
        this.tableInfo = JdbcOperate.getTableInfo(queryConnection, jdbcConfig.getTableName());
        this.key = JdbcOperate.getPrimaryKeys(queryConnection, jdbcConfig.getTableName());
        jdbcConfig.setKey(key);

        initTable();

        Assert.hasText(jdbcConfig.getSql(), "sql未设置");
        this.sql = jdbcConfig.getSql().toLowerCase()
                .replace("where", "where ( ") + " ) ";
        Assert.isTrue(sql.startsWith("select") || sql.startsWith("SELECT"), "仅支持查询sql<select | SELECT>");
        Assert.isTrue(sql.toLowerCase().contains("where"), "SQL缺少where");

        this.querySQL = sql + " and "
                + key
                + " > "
        ;
        int batchSize = jdbcConfig.getBatchSize();
        Assert.isTrue(batchSize <= 10000, "批次设置不能超过10000");

        this.endSql = " order by " + key + " asc limit 0,"
                + jdbcConfig.getBatchSize();
        this.queue = new LinkedBlockingQueue<>(1024);
        return jdbcConfig;
    }

    private Connection getConnection(String jdbcUrl) {
        int i = 0;
        while (++i < 3) {
            try {
                return DriverManager.getConnection(jdbcUrl, new Properties() {
                    private static final long serialVersionUID = 8442329719653746743L;

                    {
                        setProperty("user", jdbcConfig.getUserName());
                        setProperty("password", jdbcConfig.getPassword());
                    }
                });
            } catch (SQLException e) {
                log.error("mysql通讯链路故障", e);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException interruptedException) {
                    //
                }
            }
        }
        throw new RuntimeException("mysql connection fail.");
    }

    @SuppressWarnings("all")
    protected void init() {
        count = getConditionCount();
        thread = new Thread(this::process);
        thread.setName("jdbc-source-thread");
        thread.setUncaughtExceptionHandler(handler);
        startTime = System.currentTimeMillis();
        running = true;
        wait = true;
        thread.start();
    }

    public long getCount() {
        return count;
    }

    public void stop() {
        wait = true;
    }

    public void start() {
        synchronized (lock) {
            wait = false;
            log.info("源任务:{} 启动, thread:{} running:{} wait:{}",
                    getTaskId(), thread.getState(), running, wait);
            lock.notifyAll();
        }
    }

    public boolean getStatus() {
        return wait;
    }

    public void initLastIndex(Object lastIndex) {
        this.lastIndex = lastIndex;
    }

    protected void process() {
        while (running) {
            try {
                // task停止
                if (wait) {
                    synchronized (lock) {
                        try {
                            log.info("源任务:{} 暂停, thread:{} running:{} wait:{}",
                                    getTaskId(), thread.getState(), running, wait);
                            lock.wait();
                        } catch (InterruptedException e) {
                            //
                        }
                        if (!running || queryConnection.isClosed()) {
                            break;
                        }
                    }
                }
                boolean hasNext = false;

                log.info("查询sql:{}", querySQL + lastIndex + endSql);
                ResultSet resultSet = statement.executeQuery(querySQL + lastIndex + endSql);
                List<JdbcOperate.ColumnInfo> columns = tableDefinition.getColumns();
                GenericRecord record = new GenericRecord();

                while (resultSet.next()) {
                    hasNext = true;
                    Map<String, Object> map = new HashMap<>(16);
                    columns.forEach(columnInfo -> {
                        try {
                            Object object = resultSet.getObject(columnInfo.getPosition());
                            if (columnInfo.getName().equals(key)) {
                                lastIndex = object;
                            }
                            map.put(columnInfo.getName(), object);
                        } catch (SQLException e) {
                            throw new RuntimeException("数据读取错误");
                        }
                    });
                    record.setLastId(lastIndex);
                    record.add(map);
                }
                queue.put(new Record<T>() {
                    @Override
                    public T getValue() {
                        return (T) record;
                    }
                });
                // 放入一个空
                if (!hasNext) {
                    log.info("任务:{} 查询耗时：{}ms", getTaskId(), System.currentTimeMillis() - startTime);
                    running = false;
                    if (!queryConnection.isClosed()) {
                        queryConnection.close();
                    }
                }
            } catch (SQLException e) {
                log.error("sql error 任务:{} ", getTaskId(), e);
                running = false;
            } catch (InterruptedException e) {
                log.info("执行结束！");
                running = false;
            }
        }
    }

    protected abstract Object getTaskId();

    @Override
    public Record<T> read() throws Exception {
        return queue.take();
    }

    @Override
    public void delete(Record<T> record) throws Exception {
        String value = null;
        try {
            value = buildValue(record);
            deleteStatement.execute(value);
        } catch (Exception e) {
            log.error("sql error, 任务id:{} sql:{}", getTaskId(), value, e);
            deleteConnection.rollback();
            throw new RuntimeException(e);
        }
        deleteConnection.commit();
    }

    protected abstract String buildValue(Record<T> record);

    @Override
    public void close() throws Exception {
        if (!running) {
            if (!deleteConnection.isClosed()) {
                deleteConnection.close();
            }
            if (!queryConnection.isClosed()) {
                queryConnection.close();
            }
            if (thread != null) {
                thread.interrupt();
                thread.join();
            }
            return;
        }
        log.info("close source");
        running = false;
        try {
            if (!deleteConnection.isClosed()) {
                deleteConnection.close();
            }
            if (!queryConnection.isClosed()) {
                queryConnection.close();
            }
        } catch (SQLException e) {
            log.error("mysql connect close error.", e);
        }
        if (thread != null) {
            thread.interrupt();
            thread.join();
        }
    }

    private void initTable() throws Exception {
        // 表DDL信息
        this.tableDefinition = JdbcOperate.getTableDefinition(queryConnection, tableInfo);
        this.statement = queryConnection.createStatement();
        this.deleteStatement = deleteConnection.createStatement();
    }
}
