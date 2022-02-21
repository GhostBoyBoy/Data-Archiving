package io.github.mingyifei.manager.service;

import io.github.mingyifei.datsource.jdbc.MysqlTask;
import io.github.mingyifei.eunms.StatusEunm;
import io.github.mingyifei.manager.entity.SourceTask;
import io.github.mingyifei.manager.entity.TargetTask;
import io.github.mingyifei.manager.entity.TaskGroup;
import io.github.mingyifei.manager.model.req.Task;
import io.github.mingyifei.manager.model.req.UpdateTask;
import io.github.mingyifei.manager.repository.SourceTaskRepository;
import io.github.mingyifei.manager.repository.TargetTaskRepository;
import io.github.mingyifei.manager.repository.TaskGroupRepository;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 1:49 下午
 **/
@Slf4j
@Service
public class TaskService {

    @Resource
    private SourceTaskRepository sourceRepository;

    @Resource
    private TargetTaskRepository taskRepository;

    @Resource
    private TaskGroupRepository groupRepository;

    private static final Map<Integer, io.github.mingyifei.datsource.Task> taskMap = new ConcurrentHashMap<>();

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final Runnable shutdownThread = new Runnable() {
        @Override
        public void run() {
            taskMap.forEach((id, task) -> {
                boolean stop = task.isAllStart();
                log.info("任务停止, id:{} status:{}", id, stop);
                task.stop();
            });
        }
    };

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownThread));
    }

    public List<TaskGroup> queryAllGroup() {
        return groupRepository.queryAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateGroup(TaskGroup group) {
        if (group.getId() != null) {
            groupRepository.update(group);
            return;
        }
        groupRepository.save(group);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Integer id) {
        groupRepository.delete(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Integer id) {
        sourceRepository.delete(id);
        taskRepository.deleteBySourceId(id);
        taskMap.remove(id);
    }


    /**
     * @Description: 创建任务
     * @Param: [task]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    @Transactional(rollbackFor = Exception.class)
    public void createTask(Task task) {
        int id = sourceRepository.save(task.getSourceTask());
        task.getTargetTask().setSourceTaskId(id);
        taskRepository.save(task.getTargetTask());
    }

    /**
     * @Description: 更新任务
     * @Param: [task]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    public synchronized void updateTaskBatchSizeOrConcurrency(UpdateTask task) {
        Assert.notNull(task.getId(), "任务id为空");
        SourceTask sourceTask = sourceRepository.queryById(task.getId());
        Assert.notNull(sourceTask, "源任务不存在, id:" + task.getId());
        TargetTask targetTask = taskRepository.queryBySourceTaskId(task.getId());
        Assert.notNull(targetTask, "目标任务不存在, id:" + task.getId());

        io.github.mingyifei.datsource.Task localTask = taskMap.get(task.getId());
        if (localTask != null) {
            boolean stop = localTask.isAllStop();
            Assert.isTrue(stop, "先停止任务再更新");
        }

        boolean f = false;
        if (task.getConcurrency() != null && task.getConcurrency() > 0) {
            targetTask.setConcurrency(task.getConcurrency());
            targetTask.setUpdateTime(new Date());
            f = true;
        }
        if (task.getBatchSize() != null && task.getBatchSize() > 0) {
            sourceTask.setBatchSize(task.getBatchSize());
            sourceTask.setUpdateTime(new Date());
            f = true;
        }
        transactionTemplate.execute(status -> {
            sourceRepository.update(sourceTask);
            taskRepository.update(targetTask);
            return null;
        });

        if (localTask != null && f) {
            localTask.close();
            taskMap.remove(task.getId());
            try {
                initTask(task.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @Description: 任务初始化
     * @Param: [id]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    public synchronized void initTask(Integer id) throws Exception {
        Assert.notNull(id, "id 不能为空");

        io.github.mingyifei.datsource.Task task1 = taskMap.get(id);
        if (task1 != null) {
            boolean stop = task1.isAllStop();
            Assert.isTrue(stop, "初始化失败，存在相同运行中的任务");
            task1.close();
        }
        SourceTask sourceTask = sourceRepository.queryById(id);
        Assert.notNull(sourceTask, "SourceTask不存在");
        TargetTask targetTask = taskRepository.queryBySourceTaskId(id);
        Assert.notNull(targetTask, "TargetTask不存在");

        Map<String, Object> sourceConf = new HashMap<>(16);
        sourceConf.put("userName", sourceTask.getUserName());
        sourceConf.put("password", sourceTask.getPassword());
        sourceConf.put("jdbcUrl", sourceTask.getUrl());
        sourceConf.put("tableName", sourceTask.getTableName());
        sourceConf.put("sql", sourceTask.getSql());
        sourceConf.put("batchSize", sourceTask.getBatchSize());

        HashMap<String, Object> targetConf = new HashMap<>(16);
        targetConf.put("userName", targetTask.getUserName());
        targetConf.put("password", targetTask.getPassword());
        targetConf.put("jdbcUrl", targetTask.getUrl());
        targetConf.put("tableName", targetTask.getTableName());
        targetConf.put("interval", targetTask.getInterval());
        targetConf.put("sleep", targetTask.getSleep());
        targetConf.put("concurrentMaxThread", targetTask.getConcurrency());

        io.github.mingyifei.datsource.Task task = new MysqlTask();
        task.load(sourceConf, targetConf, id);
        CountDownLatch downLatch = new CountDownLatch(1);
        task.init((id1, sourceCount, targetCount) -> {
            try {
                downLatch.await();
            } catch (InterruptedException e) {
                //
            }
            taskMap.remove(id1);
            // 任务完成
            transactionTemplate.execute(status -> {
                SourceTask newTask = new SourceTask();
                newTask.setId(id);
                newTask.setStatus(StatusEunm.FINISH.getId());
                newTask.setTargetCount((int)targetCount);
                newTask.setSourceCount((int)sourceCount);
                sourceRepository.updateStatusAndRecord(newTask);
                return null;
            });
        });
        taskMap.put(id, task);
        transactionTemplate.execute(status -> {
            updateStatus(id, StatusEunm.STOP);
            return null;
        });
        downLatch.countDown();
    }

    /**
     * @Description: 启动任务
     * @Param: [id]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    @Transactional(rollbackFor = Exception.class)
    public synchronized void start(Integer id) {
        io.github.mingyifei.datsource.Task task = taskMap.get(id);
        Assert.notNull(task, "任务未初始化");
        boolean stop = task.isAllStop();
        Assert.isTrue(stop, "状态存在运行中");
        task.start();
        updateStatus(id, StatusEunm.START);
    }

    /**
     * @Description: 暂停任务
     * @Param: [id]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    @Transactional(rollbackFor = Exception.class)
    public synchronized void stop(Integer id) {
        io.github.mingyifei.datsource.Task task = taskMap.get(id);
        if (task == null) {
            updateStatus(id, StatusEunm.STOP);
            return;
        }

        SourceTask sourceTask = sourceRepository.queryById(id);
        if (!StatusEunm.START.getId().equals(sourceTask.getStatus())) {
            throw new RuntimeException("仅已启动可暂停");
        }

        boolean stop = task.isAllStart();
        log.info("任务停止, id:{} status:{}", id, stop);
        task.stop();
        updateStatus(id, StatusEunm.STOP);
    }

    public List<Task> queryAllTask(String group, String taskName) {
        List<SourceTask> sourceTasks = sourceRepository.queryByCondition(group, taskName);
        return sourceTasks.stream()
                .map(sourceTask -> {
                    Task task = new Task();
                    sourceTask.setPassword("");
                    task.setSourceTask(sourceTask);
                    TargetTask targetTask = taskRepository.queryBySourceTaskId(sourceTask.getId());
                    targetTask.setPassword("");
                    task.setTargetTask(targetTask);
                    return task;
                })
                .collect(Collectors.toList());
    }

    /**
     * @Description: 更新状态
     * @Param: [id, statusEunm]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    private void updateStatus(Integer id, StatusEunm statusEunm) {
        SourceTask newTask = new SourceTask();
        newTask.setId(id);
        newTask.setStatus(statusEunm.getId());
        sourceRepository.updateStatusById(newTask);
    }

}
