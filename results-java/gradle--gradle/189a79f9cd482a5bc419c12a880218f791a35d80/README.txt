commit 189a79f9cd482a5bc419c12a880218f791a35d80
Author: Hans Dockter <mail@dockter.biz>
Date:   Fri Jul 11 17:59:25 2008 +0000

    GRADLE-143
    Introduce a flag that allows a task to declare that it does not modify the project tree.
    Performance Improvement by not rebuilding the dag in a multi-task build, when the tasks declare themselves as dag neutral.
    Performance improvement by caching results of dag opertations.


    git-svn-id: http://svn.codehaus.org/gradle/gradle-core/trunk@638 004c2c75-fc45-0410-b1a2-da8352e2331b