package io.github.mingyifei.manager.repository;

import io.github.mingyifei.manager.entity.TaskGroup;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 11:23 上午
 **/
@Slf4j
@Component
public class TaskGroupRepository {

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT =
            "INSERT INTO `t_task_group`(`id`, `group_name`, `desc`, `create_time`, `update_time`) VALUES"
                    + " (:id, :groupName, :desc, :createTime, :updateTime)";

    private static final String UPDATE =
            "UPDATE `t_task_group` SET `group_name` = :groupName, `desc` = :desc, `create_time` = :createTime, `update_time` = :updateTime WHERE `id` = :id";

    private static final String DELETE = "DELETE FROM `t_task_group` WHERE `id` = :id";

    private static final String QUERY_ALL = "SELECT * FROM `t_task_group`";

    public void save(TaskGroup taskGroup){
        taskGroup.setCreateTime(new Date());
        taskGroup.setUpdateTime(new Date());
        namedParameterJdbcTemplate.update(INSERT, new BeanPropertySqlParameterSource(taskGroup));
    }

    public void update(TaskGroup taskGroup){
        Assert.notNull(taskGroup.getId(), "id不能为空");
        namedParameterJdbcTemplate.update(UPDATE, new BeanPropertySqlParameterSource(taskGroup));
    }

    public void delete(Integer id){
        Assert.notNull(id, "id不能为空");
        namedParameterJdbcTemplate.update(DELETE, new MapSqlParameterSource("id", id));
    }

    public List<TaskGroup> queryAll(){
        return namedParameterJdbcTemplate.query(QUERY_ALL, new EmptySqlParameterSource(), new BeanPropertyRowMapper<>(TaskGroup.class));
    }
}
