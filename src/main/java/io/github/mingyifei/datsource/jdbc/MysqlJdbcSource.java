package io.github.mingyifei.datsource.jdbc;

import io.github.mingyifei.datsource.Record;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 2:10 下午
 **/
@Slf4j
public class MysqlJdbcSource extends JdbcAbstractSource<GenericRecord> {

    private Integer taskId;

    @Override
    protected String buildValue(Record<GenericRecord> record) {
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ");
        builder.append(tableDefinition.getTableInfo().getTableName());
        builder.append(" where ");
        builder.append(key);
        builder.append(" in (");

        List<Map<String, Object>> dataGroup = record.getValue().getDataGroup();
        dataGroup.forEach(map -> {
            builder.append(map.get(key));
            builder.append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
