package io.github.mingyifei;

import io.github.mingyifei.manager.repository.TargetTaskRepository;
import io.github.mingyifei.manager.entity.TargetTask;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataSyncApplication.class)
public class TargetTaskRepositoryTests {

    @Autowired
    private TargetTaskRepository taskRepository;

    @Test
    public void save() {
        TargetTask targetTask = new TargetTask();
        targetTask.setConcurrency(4);
        targetTask.setType(1);
        targetTask.setSourceTaskId(2);
        targetTask.setCreateTime(new Date());
        targetTask.setUpdateTime(new Date());
        for (int i = 0; i < 10; i++) {
            taskRepository.save(targetTask);
        }
    }

    @Test
    public void update() {
        TargetTask targetTask = new TargetTask();
        targetTask.setId(2);
        targetTask.setCreateTime(new Date());
        targetTask.setUpdateTime(new Date());
        taskRepository.update(targetTask);
    }

    @Test
    public void delete() {
        taskRepository.deleteBySourceId(1);
    }

    @Test
    public void query() {
        List<TargetTask> targetTasks = taskRepository.queryAll();
        log.info("result:{}", targetTasks);
    }
}
