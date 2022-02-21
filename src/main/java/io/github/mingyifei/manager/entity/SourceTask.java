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
public class SourceTask {

    private Integer id;

    @NotBlank
    private String group;

    @NotBlank
    private String taskName;

    @NotNull
    private Integer type;

    private Integer status;

    @NotBlank
    private String url;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    @NotBlank
    private String tableName;

    @NotBlank
    private String sql;

    @NotNull
    @Min(1)
    @Max(20000)
    private Integer batchSize;

    private Integer sourceCount;

    private Integer targetCount;

    private Date createTime;

    private Date updateTime;
}
