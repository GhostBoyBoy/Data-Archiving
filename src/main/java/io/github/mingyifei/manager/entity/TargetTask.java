package io.github.mingyifei.manager.entity;

import java.util.Date;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 10:53 上午
 **/
@Data
public class TargetTask {

    private Integer id;

    private Integer sourceTaskId;

    private Integer type;

    @NotBlank
    private String url;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    @NotBlank
    private String tableName;

    @NotNull
    @Min(1)
    @Max(20)
    private Integer concurrency;

    @NotNull
    @Min(0)
    private Integer interval;

    @NotNull
    @Min(0)
    private Integer sleep;

    private Date createTime;

    private Date updateTime;
}
