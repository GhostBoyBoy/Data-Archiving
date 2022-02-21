package io.github.mingyifei.manager.entity;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 10:57 上午
 **/
@Data
public class TaskGroup {

    private Integer id;

    @NotBlank
    private String groupName;

    private String desc;

    private Date createTime;

    private Date updateTime;
}
