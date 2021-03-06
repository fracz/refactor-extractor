/*
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

package com.dangdang.ddframe.rdb.sharding;

import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dangdang.ddframe.rdb.sharding.spring.cases.WithoutNamespaceDefaultStrategyTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.WithoutNamespaceTest;

@RunWith(Suite.class)
@SuiteClasses({
    WithNamespaceAlgorithmClassTest.class,
    WithNamespaceAlgorithmClassAndPropsTest.class,
    WithNamespaceDifferentTablesTest.class,
    WithNamespaceAlgorithmExpressionTest.class,
    WithNamespaceDefaultStrategyTest.class,
    WithNamespaceBindingTablesTest.class,
    WithoutNamespaceTest.class,
    WithoutNamespaceDefaultStrategyTest.class,
    WithNamespaceDifferentTablesTest.class
    })
public class AllSpringTests {
}