package io.github.mingyifei.datsource;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 1:39 下午
 **/
public interface Record<T> {
    default T getValue() {
        return null;
    }
}
