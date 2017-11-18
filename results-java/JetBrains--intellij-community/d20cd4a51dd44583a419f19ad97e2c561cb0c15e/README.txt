commit d20cd4a51dd44583a419f19ad97e2c561cb0c15e
Author: nik <Nikolay.Chashnikov@jetbrains.com>
Date:   Mon Jul 10 10:57:12 2017 +0300

    refactoring: use classes from 'util-rt' instead of 'util' where possible in runtime modules

    This is needed to migrate 'util' to Java 8 (IDEA-174662).