package com.lxm.framework.common.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    /**
     * 分隔数组 根据每段数量分段
     *
     * @param data  被分隔的数组
     * @param count 每段数量
     * @return List
     */
    public static <T> List<List<T>> subListByCount(List<T> data, int count) {
        List<List<T>> result = new ArrayList<>();
        int size = data.size();
        if (size > 0 && count > 0) {
            int segments = size / count;
            segments = size % count == 0 ? segments : segments + 1;
            List<T> cutList;
            for (int i = 0; i < segments; i++) {
                if (i == segments - 1) {
                    cutList = data.subList(count * i, size);
                } else {
                    cutList = data.subList(count * i, count * (i + 1));
                }
                result.add(cutList);
            }
        } else {
            result.add(data);
        }
        return result;
    }

}
