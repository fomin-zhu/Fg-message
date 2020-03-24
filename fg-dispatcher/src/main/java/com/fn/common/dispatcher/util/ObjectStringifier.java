package com.fn.common.dispatcher.util;

/**
 * 对象串词器
 * @author fomin
 * @date 2019-12-07
 */
public interface ObjectStringifier {

    String toString(Object obj);

    default String toString(Object[] arr) {
        if (arr == null) return "null";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(toString(arr[i]));
        }
        return sb.append("]").toString();
    }
}
