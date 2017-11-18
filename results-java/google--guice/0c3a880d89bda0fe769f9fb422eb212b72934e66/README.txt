commit 0c3a880d89bda0fe769f9fb422eb212b72934e66
Author: Stuart McCulloch <mcculls@gmail.com>
Date:   Fri Nov 7 09:43:43 2014 -0800

    Robustness: wrap any calls to System.getProperty in case security is enabled.

    Since calling System.getProperty may throw an exception on locked-down systems.

    Note: this patch has the side-effect of making the 'guice_include_stack_traces'
    setting static, rather than querying the system property each time a module is
    installed. Making the setting static improves performance as System.getProperty
    can be a bottleneck, at the cost of being able to change the setting
    on-the-fly.

    (This is a slightly modified version of the pull request from #872.)
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=79427020