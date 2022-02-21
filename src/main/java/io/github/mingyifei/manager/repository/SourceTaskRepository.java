/*
package io.github.mingyifei.manager.repository;

import io.github.mingyifei.eunms.StatusEunm;
import io.github.mingyifei.manager.entity.SourceTask;
import CryptUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

*/
/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 11:23 上午
 **//*

@Slf4j
@Component
public class SourceTaskRepository {

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT =
            "INSERT INTO `t_source_task`(`id`, `group`, `task_name`, `type`, `status`, `url`, "
                    + "`user_name`, "
                    + "`password`, `table_name`, `sql`, `batch_size`, `source_count`, `target_count`, `create_time`, "
                    + "`update_time`) VALUES"
                    + " (:id, :group, :taskName, :type, :status, :url, :userName, :password, :tableName, :sql, "
                    + ":batchSize, :sourceCount, :targetCount, "
                    + ":createTime, :updateTime)";

    private static final String UPDATE =
            "UPDATE `t_source_task` SET `group` = :group, `task_name` = :taskName, `type` = :type, `status` = "
                    + ":status, `url` = :url, "
                    + "`user_name` = :userName, `password` = :password, `table_name` = :tableName, `sql` = :sql, "
                    + "`batch_size` = :batchSize, `source_count` = :sourceCount, `target_count` = :targetCount, "
                    + "`create_time` = :createTime, `update_time` = :updateTime WHERE `id`"
                    + " = :id";

    private static final String UPDATE_STATUS =
            "UPDATE `t_source_task` SET `status` = :status WHERE `id` = :id";

    private static final String UPDATE_STATUS_AND_RECORD =
            "UPDATE `t_source_task` SET `status` = :status, `source_count` = :sourceCount, `target_count` = "
                    + ":targetCount WHERE `id` = :id";

    private static final String DELETE = "DELETE FROM `t_source_task` WHERE `id` = :id";
    private static final String QUERY_ALL = "SELECT * FROM `t_source_task`";
    private static final String QUERY_BY_ID = "SELECT * FROM `t_source_task` WHERE `id` = :id";
    private static final String QUERY_BY_CONDITION =
            "SELECT * FROM `t_source_task` WHERE `group` = :group and `task_name` = :taskName";

    public int save(SourceTask sourceTask) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        sourceTask.setStatus(StatusEunm.NONE.getId());
        sourceTask.setCreateTime(new Date());
        sourceTask.setUpdateTime(new Date());

        sourceTask.setPassword(CryptUtil.encrypt(sourceTask.getPassword()));
        namedParameterJdbcTemplate.update(INSERT, new BeanPropertySqlParameterSource(sourceTask), keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public void update(SourceTask sourceTask) {
        Assert.notNull(sourceTask.getId(), "id不能为空");
        sourceTask.setPassword(CryptUtil.encrypt(sourceTask.getPassword()));
        namedParameterJdbcTemplate.update(UPDATE, new BeanPropertySqlParameterSource(sourceTask));
    }

    public void updateStatusById(SourceTask sourceTask) {
        Assert.notNull(sourceTask.getId(), "id不能为空");
        namedParameterJdbcTemplate.update(UPDATE_STATUS, new BeanPropertySqlParameterSource(sourceTask));
    }
    public void updateStatusAndRecord(SourceTask sourceTask) {
        Assert.notNull(sourceTask.getId(), "id不能为空");
        namedParameterJdbcTemplate.update(UPDATE_STATUS_AND_RECORD, new BeanPropertySqlParameterSource(sourceTask));
    }

    public void delete(Integer id) {
        Assert.notNull(id, "id不能为空");
        namedParameterJdbcTemplate.update(DELETE, new MapSqlParameterSource("id", id));
    }

    public SourceTask queryById(Integer id) {
        SourceTask sourceTask =
                namedParameterJdbcTemplate.queryForObject(QUERY_BY_ID, new MapSqlParameterSource("id", id),
                        new BeanPropertyRowMapper<>(SourceTask.class));
        assert sourceTask != null;
        sourceTask.setPassword(CryptUtil.decrypt(sourceTask.getPassword()));
        return sourceTask;
    }

    public List<SourceTask> queryAll() {
        return namedParameterJdbcTemplate
                .query(QUERY_ALL, new EmptySqlParameterSource(), new BeanPropertyRowMapper<>(SourceTask.class))
                .stream()
                .peek(sourceTask -> sourceTask.setPassword(CryptUtil.decrypt(sourceTask.getPassword())))
                .collect(Collectors.toList());
    }

    public List<SourceTask> queryByCondition(String group, String taskName) {
        String local = QUERY_ALL;
        if (group != null || taskName != null) {
            local += " WHERE ";
        }
        if (group != null) {
            local += "and `group` = :group ";
        }
        if (taskName != null) {
            local += "and `task_name` = :taskName ";
        }
        local = local.replace("WHERE and", "WHERE");
        local += " Order by create_time desc ";
        return namedParameterJdbcTemplate
                .query(local, new MapSqlParameterSource(new HashMap<String, Object>(4) {

                    private static final long serialVersionUID = 5571850326165752480L;

                    {
                        put("group", group);
                        put("taskName", taskName);
                    }
                }), new BeanPropertyRowMapper<>(SourceTask.class))
                .stream()
                .peek(sourceTask -> sourceTask.setPassword(CryptUtil.decrypt(sourceTask.getPassword())))
                .collect(Collectors.toList());
    }
}
*/
