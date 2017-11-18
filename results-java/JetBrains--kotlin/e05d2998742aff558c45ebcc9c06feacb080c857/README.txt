commit e05d2998742aff558c45ebcc9c06feacb080c857
Author: James Strachan <james.strachan@gmail.com>
Date:   Wed Feb 22 13:28:08 2012 +0000

    added a simple little CompilerPlugin interface and a loosely coupled loader of KDoc if its configured (via -docOutput) and its on the classpath then it can be used at the same stage as a compile & refactored KDoc to work with the compiler. Added a "docStdlib" goal to try out the kdoc on stdlib if the kdoc plugin has been built in kdoc/target/*.jar