package io.github.mingyifei.datsource.jdbc;

import io.github.mingyifei.datsource.Record;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 10:51 下午
 **/
@Slf4j
public class MysqlJdbcTarget extends JdbcAbstractTarget<GenericRecord> {

    private final MysqlJdbcSource jdbcSource;

    private volatile boolean wait;
    private volatile boolean running;
    private long startTime;
    private long count;
    public Thread thread;
    private final Object lock = new Object();

    public MysqlJdbcTarget(MysqlJdbcSource jdbcSource) {
        this.jdbcSource = jdbcSource;
    }

    public void stop() {
        this.wait = true;
    }

    public void start() {
        synchronized (lock) {
            this.wait = false;
            log.info("目标任务:{} 启动, thread:{} running:{} wait:{}",
                    jdbcSource.getTaskId(), thread.getState(), running, wait);
            lock.notifyAll();
        }
    }

    public boolean getStatus() {
        return this.wait;
    }

    /**
     * @Description: 启动
     * @Param: [addCallback]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/5
     **/
    public void init(Callback.AddCallback addCallback) throws Exception {
        this.thread = Thread.currentThread();
        this.wait = true;
        this.running = true;
        this.startTime = System.currentTimeMillis();
        int intervalCount = 0;
        while (this.running) {
            if (jdbcConfig.getInterval() > 0
                    && jdbcConfig.getSleep() > 0
                    && ++intervalCount >= jdbcConfig.getInterval()) {
                intervalCount = 0;
                TimeUnit.MILLISECONDS.sleep(jdbcConfig.getSleep());
            }
            if (this.wait) {
                synchronized (lock) {
                    log.info("目标任务:{} 暂停, thread:{} running:{} wait:{}",
                            jdbcSource.getTaskId(), thread.getState(), running, wait);
                    lock.wait();
                }
            }
            Record<GenericRecord> record = jdbcSource.read();
            if (!record.getValue().hasRecord()) {
                log.info("插入+删除耗时:{}ms", System.currentTimeMillis() - startTime);
                addCallback.close();
                break;
            }
            this.write(record);
            count += record.getValue().getRecordSize();
            // 删除
            addCallback.complete(record);
        }
    }

    @Override
    public String buildValue(Record<GenericRecord> message) throws Exception {
        List<JdbcOperate.ColumnInfo> columns = tableDefinition.getColumns();
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ");
        builder.append(tableDefinition.getTableInfo().getTableName());
        builder.append("(");
        columns.forEach(columnInfo -> builder.append(columnInfo.getName()).append(","));
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") values");
        GenericRecord record = message.getValue();
        List<Map<String, Object>> dataGroup = record.getDataGroup();
        dataGroup.forEach(stringObjectMap -> {
            builder.append("(");
            columns.forEach(columnInfo -> {
                Object value = stringObjectMap.get(columnInfo.getName());
                try {
                    if (value instanceof Timestamp) {
                        builder.append("'")
                                .append(value)
                                .append("'");
                    } else if (value instanceof String) {
                        String valueNew = (String) value;
                        if (!valueNew.contains("'") && !valueNew.contains("\\")) {
                            builder.append("'")
                                    .append(value)
                                    .append("'");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0, len = valueNew.length(); i < len; i++) {
                                char charAt = valueNew.charAt(i);
                                if (charAt == '\\') {
                                    // 校验\' \\
                                    if (i + 1 < len) {
                                        char at = valueNew.charAt(i + 1);
                                        if (at == '\'' || at == '\\') {
                                            sb.append(charAt);
                                            sb.append(at);
                                            i++;
                                            continue;
                                        }
                                    } else {
                                        // \在结尾
                                        sb.append("\\");
                                    }
                                } else if (charAt == '\'') {
                                    // '之前先添加\
                                    sb.append("\\");
                                }
                                sb.append(charAt);
                            }
                            builder.append("'")
                                    .append(sb)
                                    .append("'");
                        }
                    } else {
                        builder.append(value);
                    }
                    builder.append(",");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            builder.append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    @Override
    public void close() throws SQLException {
        log.info("close target");
        if (!running) {
            if (!connection.isClosed()) {
                connection.close();
            }
            return;
        }
        running = false;
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("mysql connect close error.", e);
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    public long getCount() {
        return count;
    }
}
