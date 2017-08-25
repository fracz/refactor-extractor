commit 7cfe64049989267767765eed4fb1ccaeae282642
Author: Petr Skoda <commits@skodak.org>
Date:   Wed Dec 28 11:24:43 2011 +0100

    MDL-30944 improve enrol_cohort plugin

    List of changes:
    * configurable unenrol action
    * cron period increased to 1 hour in order to lower server load, courses should not get out of sync often
    * CLI sync script for debugging or manual sync after configuration change
    * phpdocs fixes
    * when plugin is disabled all roles are removed, enrollments are kept
    * uninstall script
    * other bugfixing and performance improvements