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
package org.gradle.api.internal.tasks.compile;

import java.io.File;
import java.io.Serializable;

/**
 * Convenience base class for implementing <tt>JavaCompiler</tt>s that keeps
 * all required state in protected fields.
 */
public abstract class JavaCompilerSupport implements JavaCompiler, Serializable {
    protected JavaCompileSpec spec = new JavaCompileSpec();

    public JavaCompileSpec getSpec() {
        return spec;
    }

    public void setSpec(JavaCompileSpec spec) {
        this.spec = spec;
    }

    protected void listFilesIfRequested() {
        if (!spec.getCompileOptions().isListFiles()) { return; }

        StringBuilder builder = new StringBuilder();
        builder.append("Source files to be compiled:");
        for (File file : spec.getSource()) {
            builder.append('\n');
            builder.append(file);
        }
        System.out.println(builder.toString());
    }
}