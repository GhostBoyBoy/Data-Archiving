package io.github.mingyifei.datsource.jdbc;

import io.github.mingyifei.datsource.Record;
import io.github.mingyifei.datsource.Source;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 10:51 下午
 **/
@Slf4j
public abstract class JdbcAbstractTarget<T> implements Source<T> {

    protected JdbcConfig jdbcConfig;
    protected Connection connection;
    private String key;

    protected Statement statement;

    protected JdbcOperate.TableDefinition tableDefinition;

    public Object getMaxIndex() throws Exception {
        Statement statement = connection.createStatement();
        statement.execute("select max(" + key + ") from "
                + jdbcConfig.getTableName());
        ResultSet resultSet = statement.getResultSet();

        if (resultSet.next()) {
            return resultSet.getObject(1);
        }
        return null;
    }

    public boolean hasTable(String tableName) throws Exception {
        // 建表SQL
        Statement statement = connection.createStatement();
        statement.execute(JdbcOperate.buildShowTableSql());
        ResultSet resultSet = statement.getResultSet();
        while (resultSet.next()) {
            String table = resultSet.getString(1);
            if (tableName.equals(table)) {
                return true;
            }
        }
        return false;
    }

    public void createTable(String sql) throws Exception {
        // 建表SQL
        connection.createStatement().execute(sql);
    }

    @Override
    public JdbcConfig open(Map<String, Object> config) throws Exception {
        this.jdbcConfig = JdbcConfig.load(config);

        String jdbcUrl = jdbcConfig.getJdbcUrl();
        Assert.notNull(jdbcUrl, "target jdbc url未设置");

        this.key = jdbcConfig.getKey();
        Assert.notNull(key, "key为空");

        // 加载jdbc
        Class.forName(JdbcOperate.getDriverClassName(jdbcConfig.getJdbcUrl()));
        int i = 0;
        while (++i < 3) {
            try {
                this.connection = DriverManager.getConnection(jdbcUrl, new Properties() {
                    private static final long serialVersionUID = 8442329719653746743L;

                    {
                        setProperty("user", jdbcConfig.getUserName());
                        setProperty("password", jdbcConfig.getPassword());
                    }
                });
                break;
            } catch (SQLException e) {
                log.error("mysql通讯链路故障", e);
                TimeUnit.SECONDS.sleep(1);
            }
        }
        if (connection == null) {
            throw new RuntimeException("mysql connection fail.");
        }

        this.connection.setAutoCommit(false);
        return jdbcConfig;
    }

    @Override
    public void write(Record<T> record) throws Exception {
        String value = null;
        try {
            value = buildValue(record);
            statement.executeUpdate(value);
        } catch (Exception e) {
            log.warn("插入异常:{}, sql:{}", e.getMessage(), value, e);
            connection.rollback();
            if (!(e instanceof SQLIntegrityConstraintViolationException && e.getMessage().contains("Duplicate"))) {
                throw new RuntimeException(e);
            }
            Record<GenericRecord> localValue = (Record<GenericRecord>) record;
            List<Map<String, Object>> dataGroup = localValue.getValue().getDataGroup();
            dataGroup.forEach(map -> {
                GenericRecord genericRecord = new GenericRecord();
                genericRecord.add(map);
                String v = null;
                try {
                    v = buildValue(new Record<T>() {
                        @Override
                        public T getValue() {
                            return (T) genericRecord;
                        }
                    });
                    statement.executeUpdate(v);
                    connection.commit();
                } catch (Exception exception) {
                    log.warn("跳过主键冲突, sql:{}", v, exception);
                    try {
                        connection.rollback();
                    } catch (SQLException throwables) {
                        log.warn("回滚异常:", throwables);
                    }
                }
            });
            return;
        }
        connection.commit();
    }

    public abstract String buildValue(Record<T> message) throws Exception;

    public void initTableDefinition() throws Exception {
        JdbcOperate.TableInfo tableInfo = JdbcOperate.getTableInfo(connection, jdbcConfig.getTableName());
        tableDefinition = JdbcOperate.getTableDefinition(connection, tableInfo);
        statement = connection.createStatement();
    }
}
