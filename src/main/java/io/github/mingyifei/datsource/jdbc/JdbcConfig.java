package io.github.mingyifei.datsource.jdbc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 1:46 下午
 **/
@Data
@Accessors(chain = true)
public class JdbcConfig {

    private String userName;

    private String password;

    private String jdbcUrl;

    private String sql;

    private String tableName;

    private String key;

    private int batchSize = 2000;

    private int interval = 0;

    private long sleep = 0L;


    public static JdbcConfig load(Map<String, Object> map) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(new ObjectMapper().writeValueAsString(map), JdbcConfig.class);
    }
}
