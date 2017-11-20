package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * SQL构建器占位符.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLBuilderToken {

    private final String label;

    private final String value;

    private final List<Integer> indexes = new LinkedList<>();

    String toToken() {
        if (null == value) {
            return "";
        }
        Joiner joiner = Joiner.on("");
        return label.equals(value) ? joiner.join("[Token(", value, ")]") : joiner.join("[", label, "(", value, ")]");
    }

    @Override
    public String toString() {
        return null == value ? "" : value;
    }
}