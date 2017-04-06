commit 97c22fc08e1c9587307fec2e25a90345f8cf6de4
Author: Rossen Stoyanchev <rstoyanchev@vmware.com>
Date:   Fri Apr 6 14:06:23 2012 -0400

    Minor improvement in ExceptionHandlerExceptionResolver

    Moved a null check inside a protected method to give protected method
    a chance to override what happens in that case.

    Issues: SPR-9193