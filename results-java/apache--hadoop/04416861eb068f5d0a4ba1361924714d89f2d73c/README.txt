commit 04416861eb068f5d0a4ba1361924714d89f2d73c
Author: Todd Lipcon <todd@apache.org>
Date:   Thu Mar 29 21:49:37 2012 +0000

    Amend HADOOP-8212 (Improve ActiveStandbyElector's behavior when session expires)

    Amendment patch incorporates following feedback from Bikas Saha:
    - adds a new functional test for session expiration while in the standby state
    - adds a safety check in the StatCallback for session expiration
    - improves some comments


    git-svn-id: https://svn.apache.org/repos/asf/hadoop/common/branches/HDFS-3042@1307128 13f79535-47bb-0310-9956-ffa450edef68