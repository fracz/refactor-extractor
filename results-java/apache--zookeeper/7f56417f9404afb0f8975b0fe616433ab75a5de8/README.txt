commit 7f56417f9404afb0f8975b0fe616433ab75a5de8
Author: Andrew Kornev <akornev@apache.org>
Date:   Fri Apr 4 22:53:25 2008 +0000

    code refactoring for JMX enablement. Added ServerStats and QuorumStats classes. Bug fixes: OutOfMemory under heavy load, disk I/O now uses buffered streams, NIOServerCnxn.Factory shuffles the selector keys to avoid starvation. Lots of formatting: replaced tabs with whitespaces, DOS eol style converted to UNIX

    git-svn-id: https://svn.apache.org/repos/asf/hadoop/zookeeper/trunk@670914 13f79535-47bb-0310-9956-ffa450edef68