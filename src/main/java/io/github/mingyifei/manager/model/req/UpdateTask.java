package io.github.mingyifei.manager.model.req;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 4:44 下午
 **/
@Data
public class UpdateTask {

    @NotNull
    @Min(1)
    private Integer id;

    private Integer batchSize;

    private Integer concurrency;

}
