commit d5ac1b6413c1081cd5badbefa11328fcd4289193
Author: moshe weitzman <weitzman@cyrve.com>
Date:   Tue Oct 27 16:26:37 2009 +0000

    #460924 by greg.1.anderson. Site aliases may be defined in drushrc.php or on command line. These are used to run any drush command on a remote server via ssh. The improved rsync and new sql sync commands are fine examples. sql sync dumps a local or remote db, gzips, rsyncs, and then imports. We now have fast, database migration from prod => dev or vice versa.