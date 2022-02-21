package io.github.mingyifei.datsource.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 2:03 下午
 **/
@Data
public class GenericRecord {

    private List<Map<String, Object>> dataGroup = new ArrayList<>();

    private Object lastId;

    public void add(Map<String, Object> data) {
        dataGroup.add(data);
    }

    public boolean hasRecord() {
        return CollectionUtils.isNotEmpty(dataGroup);
    }

    public long getRecordSize() {
        return dataGroup.size();
    }
}
