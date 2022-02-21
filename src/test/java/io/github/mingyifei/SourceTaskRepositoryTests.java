package io.github.mingyifei;

import io.github.mingyifei.manager.repository.SourceTaskRepository;
import io.github.mingyifei.manager.entity.SourceTask;
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
public class SourceTaskRepositoryTests {

    @Autowired
    private SourceTaskRepository taskRepository;

    @Test
    public void save() {
        SourceTask sourceTask = new SourceTask();
        sourceTask.setTaskName("任务1");
        sourceTask.setSql("select * from w_order");
        sourceTask.setCreateTime(new Date());
        sourceTask.setUpdateTime(new Date());
        for (int i = 0; i < 10; i++) {
            taskRepository.save(sourceTask);
        }
    }

    @Test
    public void update() {
        SourceTask sourceTask = new SourceTask();
        sourceTask.setId(2);
        sourceTask.setTaskName("任务2");
        sourceTask.setGroup("分组test");
        sourceTask.setSql("select * from w_order");
        sourceTask.setCreateTime(new Date());
        sourceTask.setUpdateTime(new Date());
        taskRepository.update(sourceTask);
    }

    @Test
    public void delete() {
        taskRepository.delete(1);
    }

    @Test
    public void query() {
        List<SourceTask> sourceTasks = taskRepository.queryAll();
        log.info("result:{}", sourceTasks);
    }
}
