/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.util;

import lombok.extern.slf4j.Slf4j;

/**
 * SQL打印对象.
 *
 * @author zhangliang
 */
@Slf4j(topic = "Sharding-JDBC-SQL")
public final class SQLPrinter {

    private static boolean showSql;

    /**
     * 初始化.
     *
     * @param showSql 是否打印SQL
     */
    public static void init(final boolean showSql) {
        SQLPrinter.showSql = showSql;
    }

    /**
     * 打印SQL.
     *
     * @param title 日志标题
     * @param arguments 参数列表
     */
    public static void print(final String title, final Object... arguments) {
        if (showSql) {
            log.info(title, arguments);
        }
    }
}