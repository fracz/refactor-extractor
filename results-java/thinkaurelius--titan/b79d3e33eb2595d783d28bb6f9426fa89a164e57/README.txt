commit b79d3e33eb2595d783d28bb6f9426fa89a164e57
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sun Aug 10 06:43:35 2014 -0400

    Rename Faunus's Titan IOFormats

    consider the following classname (before this commit):

       com.thinkaurelius.titan.hadoop.formats.titan.cassandra.TitanCassandraInputFormat

    Repetition of "titan" has kind of gotten out of hand during
    refactoring.  This commit removes the "titan" between "formats" and
    the hbase/classandra subpackage names.

    The Faunus config files are _not_ updated in this commit.  I have
    config file changes from multiple code commits mixed in my working
    copy.  Going to commit those changes all together after the code
    rather than try to manually diff-edit properties files with `git add
    -i` (and probably get the diff-edit wrong).