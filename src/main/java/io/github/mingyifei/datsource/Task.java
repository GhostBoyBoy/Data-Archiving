package io.github.mingyifei.datsource;

import io.github.mingyifei.datsource.jdbc.Callback;
import java.util.Map;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/20 11:57 下午
 **/
public interface Task {

    /**
     * @Description: 加载配置
     * @Param: [sourceConf, targetConf, id]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    void load(Map<String, Object> sourceConf, Map<String, Object> targetConf, Integer id)throws Exception ;

    /**
     * @Description: 初始化
     * @Param: [initCallback]
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    void init(Callback.InitCallback initCallback);

    /**
     * @Description: 停止
     * @Param: []
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    void stop();

    /**
     * @Description: 启动
     * @Param: []
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    void start();

    /**
     * @Description: 是否全部停止
     * @Param: []
     * @return: boolean
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    boolean isAllStop();

    /**
     * @Description: 是否全部启动
     * @Param: []
     * @return: boolean
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    boolean isAllStart();

    /**
     * @Description: 关闭
     * @Param: []
     * @return: void
     * @Author: ming.yifei
     * @Date: 2022/1/10
     **/
    void close();
}
