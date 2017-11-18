commit e93ebeae51a7c4dc0840e2a73b3ac09a033a0c29
Author: Christopher Douglas <cdouglas@apache.org>
Date:   Mon Jun 29 07:16:56 2009 +0000

    HADOOP-6109. Change Text to grow its internal buffer exponentially, rather
    than the max of the current length and the proposed length to improve
    performance reading large values. Contributed by thushara wijeratna


    git-svn-id: https://svn.apache.org/repos/asf/hadoop/common/trunk@789242 13f79535-47bb-0310-9956-ffa450edef68