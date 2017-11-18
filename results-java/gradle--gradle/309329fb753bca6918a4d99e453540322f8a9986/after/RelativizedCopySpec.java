/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.file.copy;

import org.gradle.api.file.RelativePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RelativizedCopySpec extends DelegatingCopySpec {

    private final CopySpecInternal parent;
    private final CopySpecInternal child;

    public RelativizedCopySpec(CopySpecInternal parent, CopySpecInternal child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    CopySpecInternal getDelegateCopySpec() {
        return child;
    }

    public RelativePath getDestPath() {
        return parent.getDestPath().append(child.getDestPath());
    }

    public Collection<? extends CopySpecInternal> getAllSpecs() {
        List<RelativizedCopySpec> specs = new ArrayList<RelativizedCopySpec>();
        for (CopySpecInternal spec : child.getAllSpecs()) {
            specs.add(new RelativizedCopySpec(parent, spec));
        }
        return specs;
    }

}