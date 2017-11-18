commit dac0948cd318d9ac572b850964f4451295859422
Author: johnlenz <johnlenz@google.com>
Date:   Wed Mar 18 16:16:22 2015 -0700

    Don't propagate loose types to declared property slots. This generally improves type checking around property assignments in local scope.  This also revealed a problem with @implictCast property handling in local scope, and that is fixed here as well.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=88979120