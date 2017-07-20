commit 79d1bc7ef8b830787168fc25dcb893c9b133c9c0
Author: BeezyT <timo@ezdesign.de>
Date:   Tue Apr 10 14:49:16 2012 +0000

    refs #3073

     * graph views of data tables always request data without aggregate rows
     * csv export js fix: includeAggregateRows => include_aggregate_rows
     * the config menu is more verbose now (added new lang strings rather than reusing existing ones for low population - this way translators will notice the change)
     * refactoring to reduce duplication: label filter becomes a data table manipulator
     * integration test for include_aggregate_rows parameter

    git-svn-id: http://dev.piwik.org/svn/trunk@6182 59fd770c-687e-43c8-a1e3-f5a4ff64c105