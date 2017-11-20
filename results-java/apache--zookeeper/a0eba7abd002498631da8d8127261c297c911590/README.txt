commit a0eba7abd002498631da8d8127261c297c911590
Author: Benedict Jin <benedictjin2016@gmail.com>
Date:   Sun Jun 25 15:16:07 2017 -0700

    ZOOKEEPER-2816: Code refactoring for `ZK_SERVER` module

    * Fix spell issues
    * Merge exceptions with `|` character
    * Remove unnecessary boxing
    * Remove unused import
    * Using enhanced `for` loop
    * Using `LinkedList` for removing duplicates ACL

    Author: asdf2014 <1571805553@qq.com>

    Reviewers: Michael Han <hanm@apache.org>

    Closes #288 from asdf2014/zk_quorum