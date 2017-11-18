commit 5a9ed8ef8590190deb8085c33984906bc4e7190b
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Nov 6 08:20:32 2014 -0500

    Porting titan-hadoop to TP3

    This commit is a major and incomplete refactoring of the
    Titan-Hadoop/Faunus code.  Since most of Titan-Hadoop/Faunus's
    functions are now implemented by TP3 modules, this commit involves a
    vast reduction in scope for the Titan-Hadoop module.  This commit
    doesn't finish the job.  HBase support is missing, vertex query
    support is gone, reindex is unsupported, and the tests are deleted;
    there are some lower-priority TODOs coming in as part of the diff.