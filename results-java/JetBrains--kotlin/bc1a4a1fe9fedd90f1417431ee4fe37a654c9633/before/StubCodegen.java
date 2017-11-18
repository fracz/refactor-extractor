/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.codegen;

import org.jetbrains.asm4.MethodVisitor;

import static org.jetbrains.jet.codegen.CodegenUtil.generateMethodThrow;
import static org.jetbrains.jet.codegen.CodegenUtil.generateThrow;

/**
 * @author Stepan Koltsov
 */
public class StubCodegen {
    private static final String STUB_EXCEPTION = "java/lang/RuntimeException";
    private static final String STUB_EXCEPTION_MESSAGE = "Stubs are for compiler only, do not add them to runtime classpath";

    private StubCodegen() {
    }

    public static void generateStubThrow(MethodVisitor mv) {
        generateThrow(mv, STUB_EXCEPTION, STUB_EXCEPTION_MESSAGE);
    }

    public static void generateStubCode(MethodVisitor mv) {
        generateMethodThrow(mv, STUB_EXCEPTION, STUB_EXCEPTION_MESSAGE);
    }
}