/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 组件接口.
 *
 * @author gaohongtao
 */
// TODO 泛型的javadoc
// TODO ComponentResultSet是什么
public interface ComponentResultSet<T> extends ResultSet {

    // TODO 注释
    // TODO 接口参数不需要加final
    void init(final T preResultSet) throws SQLException;
}