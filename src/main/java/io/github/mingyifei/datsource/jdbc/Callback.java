package io.github.mingyifei.datsource.jdbc;

import io.github.mingyifei.datsource.Record;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/21 10:50 上午
 **/
public interface Callback {

    interface AddCallback {
        void close() throws Exception;

        void complete(Record<GenericRecord> record) throws Exception;

    }

    interface InitCallback {

        void complete(Integer taskId, long sourceCount, long targetCount);
    }
}
