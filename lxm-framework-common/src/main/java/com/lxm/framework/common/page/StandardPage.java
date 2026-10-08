package com.lxm.framework.common.page;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/1/30
 * @Describe
 **/
public interface StandardPage<T> extends Serializable {


    void putRecords(List<T> records);

    void putTotal(long total);

    void putSize(long size);

    void putCurrent(long current);

    String[] getAscs();

    String[] getDescs();

    long getCurrent();

    long getSize();
}
