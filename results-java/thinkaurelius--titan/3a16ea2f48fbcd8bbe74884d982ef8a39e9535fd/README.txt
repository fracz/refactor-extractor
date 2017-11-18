commit 3a16ea2f48fbcd8bbe74884d982ef8a39e9535fd
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Jun 12 21:09:30 2014 -0400

    Refactoring titan-hadoop for multiversion support

    This is an attempt to refactor the titan-hadoop module to
    transparently support both Hadoop1 and Hadoop2 using a reflective shim
    loader, similar to the way Hive and titan-hbase hide details related
    to the specific version of Hadoop and HBase, respectively.

    However, this is probably going to be more trouble than it's worth.
    There's too much API leakage in titan-hadoop-core; besides Hadoop's
    Counter class sustaining incompatible changes between versions,
    several of the test classes wont even load in a Hadoop1 CLASSPATH
    after being compiled against Hadoop2.  Instead of spending a bunch of
    time trying to isolate every incompatible change and move it into the
    shims (which is possible but time consuming), I'm probably going to
    just produce two different artifacts, one with a hadoop1 classifier
    and the other with a hadoop2 classifier.