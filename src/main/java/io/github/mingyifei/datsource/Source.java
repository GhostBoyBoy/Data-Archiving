package io.github.mingyifei.datsource;

import io.github.mingyifei.datsource.jdbc.JdbcConfig;
import java.util.Map;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 1:37 下午
 **/
public interface Source<T> extends AutoCloseable {

    JdbcConfig open(final Map<String, Object> config) throws Exception;

    default void write(Record<T> record) throws Exception{

    }

    default Record<T> read() throws Exception{
        return null;
    }

    default void delete(Record<T> record) throws Exception {

    }
}
