package io.github.mingyifei.manager.model.req;

import io.github.mingyifei.manager.entity.SourceTask;
import io.github.mingyifei.manager.entity.TargetTask;
import javax.validation.Valid;
import lombok.Data;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 1:51 下午
 **/
@Data
public class Task {

    @Valid
    private SourceTask sourceTask;

    @Valid
    private TargetTask targetTask;
}
