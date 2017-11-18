/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.tasks.diagnostics;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.gradle.api.Project;
import org.gradle.api.dependencies.ConfigurationResolver;
import org.gradle.api.dependencies.ResolveInstruction;
import org.gradle.api.dependencies.report.IvyDependencyGraph;
import org.gradle.api.filter.FilterSpec;
import org.gradle.api.internal.dependencies.DependencyManagerInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.util.WrapUtil;
import org.gradle.util.Matchers;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(JMock.class)
public class DependencyReportTaskTest {
    private final JUnit4Mockery context = new JUnit4Mockery();
    private DependencyReportRenderer renderer;
    private Project project;
    private DependencyReportTask task;

    @Before
    public void setup() {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        project = context.mock(ProjectInternal.class);
        renderer = context.mock(DependencyReportRenderer.class);

        context.checking(new Expectations() {{
            allowing(project).absolutePath("list");
            will(returnValue(":path"));
        }});

        task = new DependencyReportTask(this.project, "list");
        task.setRenderer(renderer);
    }

    @Test
    public void isDagNeutral() {
        assertTrue(task.isDagNeutral());
    }

    @Test
    public void passesEachProjectConfigurationToRenderer() throws IOException {
        final DependencyManagerInternal dependencyManager = context.mock(DependencyManagerInternal.class);
        final ConfigurationResolver configuration1 = context.mock(ConfigurationResolver.class, "Configuration1");
        final ConfigurationResolver configuration2 = context.mock(ConfigurationResolver.class, "Configuration2");
        final ResolveReport report = new ResolveReport(new DefaultModuleDescriptor(new ModuleRevisionId(new ModuleId("org", "mod"), "rev"), "status", null));

        final FilterSpec<ResolveInstruction> resolveInstructionMatcher = new FilterSpec<ResolveInstruction>() {
            public boolean isSatisfiedBy(ResolveInstruction element) {
                return element.isFailOnResolveError() == false;
            }
        };

        context.checking(new Expectations() {{
            allowing(project).getDependencies();
            will(returnValue(dependencyManager));

            allowing(dependencyManager).getConfigurations();
            will(returnValue(WrapUtil.toList(configuration2, configuration1)));

            allowing(configuration1).getName();
            will(returnValue("config1"));

            allowing(configuration2).getName();
            will(returnValue("config2"));

            Sequence resolve = context.sequence("resolve");
            Sequence render = context.sequence("render");

            one(configuration1).resolveAsReport(with(Matchers.modifierMatcher(resolveInstructionMatcher)));
            inSequence(resolve);
            will(returnValue(report));

            one(renderer).startConfiguration(configuration1);
            inSequence(render);

            one(renderer).render(with(aNonNull(IvyDependencyGraph.class)));
            inSequence(render);

            one(renderer).completeConfiguration(configuration1);
            inSequence(render);

            one(configuration2).resolveAsReport(with(Matchers.modifierMatcher(resolveInstructionMatcher)));
            inSequence(resolve);
            will(returnValue(report));

            one(renderer).startConfiguration(configuration2);
            inSequence(render);

            one(renderer).render(with(aNonNull(IvyDependencyGraph.class)));
            inSequence(render);

            one(renderer).completeConfiguration(configuration2);
            inSequence(render);
        }});

        task.generate(project);
    }


}