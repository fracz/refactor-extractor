commit 359b3e86089f8e709caa3675864428bdaac2eb33
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Sep 28 16:34:16 2010 +0000

    Added more put... methods to LogBuffer. Also refactored and use Number (something I presume
    the JVM can optimize into using primitives). If it can't do that then we'll change it later on.


    git-svn-id: https://svn.neo4j.org/components/kernel/trunk@5766 0b971d98-bb2f-0410-8247-b05b2b5feb2a