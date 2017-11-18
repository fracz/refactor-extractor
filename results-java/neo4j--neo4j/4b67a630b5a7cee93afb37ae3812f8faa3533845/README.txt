commit 4b67a630b5a7cee93afb37ae3812f8faa3533845
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Sep 28 16:34:16 2010 +0000

    Added more put... methods to LogBuffer. Also refactored and use Number (something I presume
    the JVM can optimize into using primitives). If it can't do that then we'll change it later on.


    git-svn-id: https://svn.neo4j.org/components/kernel/trunk@5766 0b971d98-bb2f-0410-8247-b05b2b5feb2a