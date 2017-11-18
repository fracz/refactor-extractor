/*
 * Copyright 2011 the original author or authors.
 *
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
 */
package org.gradle.api.tasks.compile;

import org.gradle.api.internal.tasks.compile.CompilerChooser;
import org.gradle.api.internal.tasks.compile.JavaCompiler;

public class AntOrForkingCompilerChooser implements CompilerChooser {
    private final JavaCompiler antCompiler;
    private final JavaCompiler forkingCompiler;

    public AntOrForkingCompilerChooser(JavaCompiler antCompiler, JavaCompiler forkingCompiler) {
        this.antCompiler = antCompiler;
        this.forkingCompiler = forkingCompiler;
    }

    public JavaCompiler choose(CompileOptions options) {
        if (options.isFork() && !options.getForkOptions().isUseAntForking()) {
            return forkingCompiler;
        }
        return antCompiler;
    }
}