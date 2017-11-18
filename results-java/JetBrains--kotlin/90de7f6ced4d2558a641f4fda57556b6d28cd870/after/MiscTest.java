/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.k2js.test;

import org.mozilla.javascript.JavaScriptException;

/**
 * @author Pavel Talanov
 *         <p/>
 *         This class contains tests that do not fall in any particular category
 *         most probably because that functionality has very little support
 */
public final class MiscTest extends AbstractExpressionTest {


    public MiscTest() {
        super("misc/");
    }

    public void testLocalPropertys() throws Exception {
        runFunctionOutputTest("localProperty.jet", "foo", "box", 50);
    }

    public void testIntRange() throws Exception {
        checkFooBoxIsTrue("intRange.kt");
    }


    public void testSafecallComputesExpressionOnlyOnce() throws Exception {
        checkFooBoxIsTrue("safecallComputesExpressionOnlyOnce.kt");
    }

    public void testClassWithoutNamespace() throws Exception {
        runFunctionOutputTest("classWithoutNamespace.kt", "Anonymous", "box", true);
    }

    public void testIfElseAsExpressionWithThrow() throws Exception {
        try {
            checkFooBoxIsTrue("ifAsExpressionWithThrow.kt");
            fail();
        } catch (JavaScriptException e) {

        }
    }
}