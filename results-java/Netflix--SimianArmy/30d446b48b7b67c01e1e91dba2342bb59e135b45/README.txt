commit 30d446b48b7b67c01e1e91dba2342bb59e135b45
Author: Ali Basiri <abasiri@netflix.com>
Date:   Wed Apr 2 23:19:13 2014 -0700

    Refactor reading of chaos monkey properties and defaults values

    Refactoring out the reading of 'enabled', 'maxTerminationsPerDay', and 'probability' into a separate method. The method also handles reading the default values from the group level if one doesn't exist at the named group level. The reason for the refactor is to override the way defaults are read internally at netflix.