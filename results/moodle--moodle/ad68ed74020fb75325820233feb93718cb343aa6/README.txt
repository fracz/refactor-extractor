commit ad68ed74020fb75325820233feb93718cb343aa6
Author: Petr Skoda <commits@skodak.org>
Date:   Wed Dec 28 17:21:59 2011 +0100

    MDL-29684 improve enrol_meta plugin

    List of changes:
    * configurable unenrol action
    * new setting for synchronisation of all enrolled users or users with at least one role
    * cron period increased to 1 hour in order to lower server load, courses should not get out of sync often
    * CLI sync script for debugging or manual sync
    * phpdocs fixes
    * when plugin is disabled all roles are removed, enrollments are kept
    * uninstall script
    * other bugfixing