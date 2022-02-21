/*
package io.github.mingyifei.manager.controller;

import io.github.mingyifei.manager.entity.TaskGroup;
import io.github.mingyifei.manager.model.req.Task;
import io.github.mingyifei.manager.model.req.UpdateTask;
import io.github.mingyifei.manager.service.TaskService;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

*/
/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 2:38 下午
 **//*

@Slf4j
@RestController
@RequestMapping("/archive/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @GetMapping("/queryAllGroup")
    public JsonResult<List<TaskGroup>> queryAllGroup() {
        return JsonResult.success(taskService.queryAllGroup());
    }

    @PostMapping("/saveOrUpdateGroup")
    public JsonResult<Void> saveOrUpdateGroup(@RequestBody @Validated TaskGroup group) {
        taskService.saveOrUpdateGroup(group);
        return JsonResult.success();
    }

    @GetMapping("/deleteGroup")
    public JsonResult<Void> deleteGroup(@RequestParam(value = "id") Integer id) {
        taskService.deleteGroup(id);
        return JsonResult.success();
    }

    @GetMapping("/deleteTask")
    public JsonResult<Void> deleteTask(@RequestParam(value = "id") Integer id) {
        taskService.deleteTask(id);
        return JsonResult.success();
    }

    @PostMapping("/createTask")
    public JsonResult<Void> createTask(@RequestBody @Validated Task task) {
        taskService.createTask(task);
        return JsonResult.success();
    }

    @PostMapping("/updateTaskBatchSizeOrConcurrency")
    public JsonResult<Void> updateTaskBatchSizeOrConcurrency(@RequestBody @Validated UpdateTask task) {
        taskService.updateTaskBatchSizeOrConcurrency(task);
        return JsonResult.success();
    }

    @GetMapping("/initTask")
    public JsonResult<Void> initTask(@RequestParam(value = "id") Integer id) {
        try {
            taskService.initTask(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return JsonResult.success();
    }

    @GetMapping("/startTask")
    public JsonResult<Void> start(@RequestParam(value = "id") Integer id) {
        taskService.start(id);
        return JsonResult.success();
    }

    @GetMapping("/stopTask")
    public JsonResult<Void> stop(@RequestParam(value = "id") Integer id) {
        taskService.stop(id);
        return JsonResult.success();
    }

    @GetMapping("/queryAllTask")
    public JsonResult<List<Task>> queryAllTask(@RequestParam(value = "group", required = false) String group,
                                               @RequestParam(value = "taskName", required = false) String taskName) {
        return JsonResult.success(taskService.queryAllTask(group, taskName));
    }
}
*/
