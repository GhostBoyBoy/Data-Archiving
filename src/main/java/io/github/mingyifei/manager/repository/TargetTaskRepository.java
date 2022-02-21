package io.github.mingyifei.manager.repository;

import io.github.mingyifei.manager.entity.TargetTask;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
public class TargetTaskRepository {

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT =
            "INSERT INTO `t_target_task`(`id`, `source_task_id`, `type`, `url`, `user_name`, `password`, `table_name`,"
                    + "`concurrency`, `interval`, `sleep`, `create_time`, `update_time`) VALUES"
                    + " (:id, :sourceTaskId, :type, :url, :userName, :password, :tableName, "
                    + ":concurrency, :interval, :sleep, :createTime, :updateTime)";

    private static final String UPDATE =
            "UPDATE `t_target_task` SET `source_task_id` = :sourceTaskId, `type` = :type, `url` = :url, `user_name` ="
                    + " :userName, `password` = :password, `table_name` = :tableName, `concurrency` = :concurrency, "
                    + "`interval` = :interval, `sleep` = :sleep, "
                    + "`create_time` = :createTime, `update_time` = :updateTime WHERE `id` = :id";

    private static final String DELETE = "DELETE FROM `t_target_task` WHERE `source_task_id` = :id";
    private static final String QUERY_ALL = "SELECT * FROM `t_target_task`";
    private static final String QUERY_BY_SOURCE_TASK_ID =
            "SELECT * FROM `t_target_task` WHERE `source_task_id` = :sourceTaskId";

    public void save(TargetTask targetTask) {
        targetTask.setCreateTime(new Date());
        targetTask.setUpdateTime(new Date());
        targetTask.setPassword(targetTask.getPassword());
        namedParameterJdbcTemplate.update(INSERT, new BeanPropertySqlParameterSource(targetTask));
    }

    public void update(TargetTask targetTask) {
        Assert.notNull(targetTask.getId(), "id不能为空");
        targetTask.setPassword(targetTask.getPassword());
        namedParameterJdbcTemplate.update(UPDATE, new BeanPropertySqlParameterSource(targetTask));
    }

    public void deleteBySourceId(Integer id) {
        Assert.notNull(id, "id不能为空");
        namedParameterJdbcTemplate.update(DELETE, new MapSqlParameterSource("id", id));
    }

    public List<TargetTask> queryAll() {
        return namedParameterJdbcTemplate
                .query(QUERY_ALL, new EmptySqlParameterSource(), new BeanPropertyRowMapper<>(TargetTask.class))
                .stream()
                .peek(targetTask -> targetTask.setPassword(targetTask.getPassword()))
                .collect(Collectors.toList());
    }

    public TargetTask queryBySourceTaskId(Integer sourceTaskId) {
        TargetTask targetTask = namedParameterJdbcTemplate
                .queryForObject(QUERY_BY_SOURCE_TASK_ID, new MapSqlParameterSource("sourceTaskId", sourceTaskId),
                        new BeanPropertyRowMapper<>(TargetTask.class));
        assert targetTask != null;
        targetTask.setPassword(targetTask.getPassword());
        return targetTask;
    }
}
