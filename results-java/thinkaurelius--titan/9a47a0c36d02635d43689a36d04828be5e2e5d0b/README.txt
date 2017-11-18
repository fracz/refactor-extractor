commit 9a47a0c36d02635d43689a36d04828be5e2e5d0b
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu May 1 17:42:24 2014 -0400

    Rename c.t.faunus package to c.t.titan.hadoop

    Running `grep -Ir com.thinkaurelius.faunus data src` from the
    titan-hadoop module now only generates hits in the index.html template
    we use for Github Pages and gremlin.{sh,bat}.  Marko's working on
    gremlin.sh, so I'm leaving that one alone for the moment.  I think I
    also got all the directory renames that weren't covered by groovy and
    java refactoring (e.g. src/main/resources/c/t/faunus/version.txt is
    now .../titan/hadoop/version.txt).

    This commit does not completely replace references to the name
    "faunus" with "Titan/Hadoop", it just changes the name of the package
    where the code lives.  Unit and integration tests pass on my machine.