commit cfb0fafc7d5f98ec94cd41ffaaf28af80489dc6c
Author: cfarrugia <chris@yobetit.com>
Date:   Tue Dec 1 23:30:01 2015 +0100

    #113 Event Driven Architecture

    Adds various changes including :
    - Fixes to Javadoc
    - Test refactoring and improvements
    - Refactored EventDispatcher to be immutable
    - Removed DynamicRouter interface since it not needed
    - Renamed Channel to a more appropriate name - Handler