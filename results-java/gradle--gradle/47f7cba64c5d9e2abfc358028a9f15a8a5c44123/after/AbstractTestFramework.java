package org.gradle.api.tasks.testing;

import org.gradle.api.Project;

import java.util.List;

/**
 * @author Tom Eyckmans
 */
public abstract class AbstractTestFramework implements TestFramework {

    public static final String USE_OF_CORRECT_TEST_FRAMEWORK =
        "Make sure the correct TestFramework is in use. \n" +
        "            - Call useJUnit(), useTestNG() or useTestFramework(<your own TestFramework implementation class>) as first statement in the test { } block. \n" +
        "            - Set the test.framework.default property in a gradle.properties file ";

    protected String name;

    protected AbstractTestFramework(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void configureDefaultIncludesExcludes(Project project, Test testTask) {
        final List includes = testTask.getIncludes();
        if ( includes == null || includes.isEmpty() ) {
            testTask.include("**/*Tests.class", "**/*Test.class");

        }
        final List excludes = testTask.getExcludes();
        if ( excludes == null || excludes.isEmpty() ) {
            testTask.exclude("**/Abstract*.class");
        }
    }
}