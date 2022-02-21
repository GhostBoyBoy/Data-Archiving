package io.github.mingyifei.eunms;

import lombok.Getter;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2022/1/10 7:15 下午
 **/
@Getter
@SuppressWarnings("all")
public enum StatusEunm {
    NONE(0, "待初始化"),
    START(1, "已启动"),
    STOP(2, "已停止"),
    FINISH(3, "已完成"),
    ;
    private Integer id;
    private String desc;

    StatusEunm(Integer id, String desc) {
        this.id = id;
        this.desc = desc;
    }
}
