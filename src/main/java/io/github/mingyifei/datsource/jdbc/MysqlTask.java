package io.github.mingyifei.datsource.jdbc;

import io.github.mingyifei.datsource.Record;
import io.github.mingyifei.datsource.Task;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/20 11:08 下午
 **/
@Slf4j
public class MysqlTask implements Task {
    private MysqlJdbcSource jdbcSource;
    private final List<MysqlJdbcTarget> jdbcTargets = new ArrayList<>();
    private ThreadPoolExecutor poolExecutor;
    private final AtomicInteger count = new AtomicInteger(1);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private boolean status;

    @Override
    public void load(Map<String, Object> sourceConf, Map<String, Object> targetConf, Integer id) throws Exception {
        log.info("开始配置初始化");
        jdbcSource = new MysqlJdbcSource();
        jdbcSource.setTaskId(id);
        JdbcConfig jdbcConfig = jdbcSource.open(sourceConf);
        targetConf.put("key", jdbcConfig.getKey());
        // targetConf.put("tableName", jdbcConfig.getTableName());

        // 初始化目标表和源表
        String targetTableName = (String) targetConf.get("tableName");
        initSourceAndTarget(targetConf, jdbcSource, targetTableName,
                jdbcSource.getCreateTableSql((String) sourceConf.get("tableName")));

        Integer maxThread = (Integer) targetConf.getOrDefault("concurrentMaxThread", 1);
        poolExecutor =
                new ThreadPoolExecutor(maxThread, maxThread, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024),
                        new DefaultThreadFactory("target-thread"));
        for (int i = 0; i < maxThread; i++) {
            MysqlJdbcTarget target = createTarget(jdbcSource, targetConf);
            jdbcTargets.add(target);
        }
        log.info("配置初始化完成");
    }

    @Override
    @SuppressWarnings("all")
    public synchronized void init(Callback.InitCallback initCallback) {
        if (status) {
            return;
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> initExecute(future, initCallback));
        thread.setDaemon(false);
        thread.setName("task-init");
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error("任务异常，thread:{} msg:{}", t.getName(), e.getMessage(), e);
            }
        });
        thread.start();
        try {
            Boolean b = future.get();
            if (Boolean.TRUE.equals(b)) {
                log.info("任务准备就绪，数据源连接初始化完毕，task-init线程挂起, id:{}", jdbcSource.getTaskId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initExecute(CompletableFuture<Boolean> future,
                             Callback.InitCallback initCallback) {
        log.info("开始初始化任务");
        jdbcSource.init();

        jdbcTargets.forEach(mysqlJdbcTarget -> poolExecutor.execute(() -> {
            try {
                mysqlJdbcTarget.init(new Callback.AddCallback() {

                    @Override
                    public void close() throws Exception {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void complete(Record<GenericRecord> record) throws Exception {
                        jdbcSource.delete(record);
                    }
                });
            } catch (Exception e) {
                log.warn("目标任务执行异常.", e);
                if (e instanceof InterruptedException) {
                    return;
                }
                countDownLatch.countDown();
            }
        }));
        try {
            getTaskCount();
            status = true;
            future.complete(true);
            countDownLatch.await();
        } catch (InterruptedException e) {
            //
        }
        jdbcTargets.forEach(mysqlJdbcTarget -> {
            try {
                Thread.State state = mysqlJdbcTarget.thread.getState();
                if (Thread.State.WAITING.equals(state)) {
                    mysqlJdbcTarget.close();
                } else {
                    while (!Thread.State.WAITING.equals(mysqlJdbcTarget.thread.getState())) {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                }
                log.info("目标任务状态：{}", mysqlJdbcTarget.thread.getState());
                mysqlJdbcTarget.close();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    return;
                }
                log.error("task close error, id:{}", jdbcSource.getTaskId());
            }
        });
        try {
            jdbcSource.close();
        } catch (Exception e) {
            log.error("source close error.id:{}", jdbcSource.getTaskId());
        }
        long sourceCount = jdbcSource.getCount();
        long targetCount = jdbcTargets.stream()
                .map(MysqlJdbcTarget::getCount)
                .reduce(Long::sum)
                .orElse(0L);
        log.info("任务执行完成, 任务id:{} sourceCount:{}, targetCount:{}",
                jdbcSource.getTaskId(), sourceCount, targetCount);
        poolExecutor.shutdownNow();
        initCallback.complete(jdbcSource.getTaskId(), sourceCount, targetCount);
    }

    public long getTaskCount() throws InterruptedException {
        if (count.get() > 10) {
            log.error("任务初始化失败");
            Thread.currentThread().interrupt();
        }
        long taskCount = poolExecutor.getTaskCount();
        if (jdbcTargets.size() == taskCount) {
            return taskCount;
        }
        count.incrementAndGet();
        TimeUnit.MILLISECONDS.sleep(500);
        getTaskCount();
        return taskCount;
    }

    @Override
    public void stop() {
        jdbcSource.stop();
        jdbcTargets.forEach(MysqlJdbcTarget::stop);
    }

    @Override
    public void start() {
        jdbcSource.start();
        jdbcTargets.forEach(MysqlJdbcTarget::start);
    }

    @Override
    public boolean isAllStop() {
        boolean status = jdbcSource.getStatus();
        // 全部等待
        boolean allMatch = jdbcTargets.stream().allMatch(MysqlJdbcTarget::getStatus);
        return status && allMatch;
    }

    @Override
    public boolean isAllStart() {
        boolean status = !jdbcSource.getStatus();
        // 全部启动
        boolean noneMatch = jdbcTargets.stream().noneMatch(MysqlJdbcTarget::getStatus);
        return status && noneMatch;
    }

    @Override
    public void close() {
        countDownLatch.countDown();
    }

    private static MysqlJdbcTarget createTarget(MysqlJdbcSource jdbcSource, Map<String, Object> targetConf)
            throws Exception {
        MysqlJdbcTarget target = new MysqlJdbcTarget(jdbcSource);
        target.open(targetConf);
        target.initTableDefinition();
        return target;
    }

    private void initSourceAndTarget(Map<String, Object> targetConf, MysqlJdbcSource jdbcSource, String tableName,
                                     String tableSql) throws Exception {
        MysqlJdbcTarget target = new MysqlJdbcTarget(jdbcSource);
        target.open(targetConf);

        // 同步表结构
        syncTable(tableName, tableSql, target);

        // 初始化表
        target.initTableDefinition();

        /*Object maxIndex = target.getMaxIndex();
        if (maxIndex != null) {
            jdbcSource.initLastIndex(maxIndex);
        }*/

        target.close();
    }

    private void syncTable(String tableName, String tableSql, MysqlJdbcTarget target) throws Exception {
        boolean hasTable = target.hasTable(tableName);
        if (!hasTable) {
            // 替换
            int indexOf = tableSql.indexOf("(");
            String end = tableSql.substring(indexOf);
            tableSql = "CREATE TABLE `" + tableName + "` " + end;
            log.info("目标数据源表创建 {}", tableSql);
            target.createTable(tableSql);
        }
    }
}
