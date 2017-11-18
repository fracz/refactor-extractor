commit e5c3421c60934d3fc58b45f69b35884da29eaf60
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Aug 28 22:17:49 2015 -0400

    Upgrade from Cassandra 2.0.8 to 2.1.8

    This was substantially more invasive than 2.0.x patchlevel upgrades.

    * C*2.1 is incompatible with Guava 15.  Upgraded to 16 to match C*2.1.
      I assume this broke HBase 0.94 due to experiencing
      https://issues.apache.org/jira/browse/HBASE-9667 last time I tried
      upgrading Guava.  #854 targeting Guava 18 is still open at the time
      of this commit, but a bunch of stuff broke during the C*2.1 upgrade,
      so I wanted to make the smallest possible Guava version move
      (15->16) in this commit and stabilize titan-cassandra before trying
      to go to 18.

    * C*2.1 uses JNA 4.  Upgrading C* also introduced dependency
      convergence failures for it.unimi.dsi:fastutil,
      clearspring.analytics:stream, and commons-math3 (but not
      commons-math).  Added dependencyManagement stanzas to use the newer
      version in each case.

    * C*2.1 includes some CQL-friendly internals refactoring, e.g. the
      move from Column to Cell and the introduction of the sparse-vs-dense
      column idiom.  It also removed the mostly useless type parameters on
      Token and IPartitioner.  These changes required minor tweaks in the
      thrift/astyanax store and more extensive changes in the embedded
      store.

    * C*2.1 uses logback.  logback ships with its own slf4j binding (which
      took precedence over the log4j-slf4j12 binding due to an accident of
      classpath ordering when I first upgraded the C* version).  Titan
      currently uses log4j for all of its tests, and if we eventually do
      move to logback, we should do it consistently throughout Titan; I
      don't want a mixture of logback.xml and log4j.properties files
      scattered throughout the submodules and living together indefinitely
      on a major branch.  I had to explicitly redeclare the slf4j-log4j12
      artifact and the logback-classic artifact that contains logback's
      slf4j binding in titan-cassandra/pom.xml (in that order) to force
      the log4j12 binding to take precedence over the logback binding.

    * C*2.1 has retired several config keys in our testing cassandra.yaml
      and refuses to start if they are present:

      * memtable_flush_queue_size
      * in_memory_compaction_limit_in_mb
      * multithreaded_compaction
      * compaction_preheat_key_cache
      * preheat_kernel_page_cache

      I deleted these keys.  I think we were using the default value in
      each case anyway.

    * C*2.1 uses JAMM 0.3.0 (older C* versions used 0.2.5).  This required
      updating the -javaagent JVM parameter in our shell scripts.

    * C*2.1 uses a library called airline for command-line argument
      parsing in nodetool (maybe other commands too?).  airline does not
      get pulled in automatically as a dependency of titan-cassandra.  I
      explicitly declared it as a dependency on titan-dist at version 0.6,
      the version that shipped in C*2.1.8's distro.

    * Merged miscellaneous upstream changes to the C* shell scripts that
      we include in the Titan zipfiles.  Aside from accounting for
      nodetool's main class getting renamed, these changes seemed like
      mostly quoting fixes and environment sanity checks (like printing a
      helpful message when the script can't find the java binary).  I
      excluded the shell script changes related to logback, keeping the C*
      daemon started by titan.sh on log4j for now.

    I updated the versions matrix to list Titan 1.0.0 as compatible with
    C* 1.2, 2.0, and 2.1.  Only the thrift/astyanax adapters are really
    compatible with 1.2 and 2.0 on the server-side; the embedded adapter
    is now closely wedded to 2.1 and cannot compile or run using older
    versions of the C* jar.  This is difficult to communicate in the
    matrix, and I would rather emphasize the thrift/astyanax adapter
    compatibility at the expense of clarity around the embedded adapter,
    since the latter is unsupported.

    All of the titan-cassandra tests passed as of this commit.

    Fixes #1104