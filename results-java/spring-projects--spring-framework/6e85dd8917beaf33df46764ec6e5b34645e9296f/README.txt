commit 6e85dd8917beaf33df46764ec6e5b34645e9296f
Author: Rossen Stoyanchev <rstoyanchev@vmware.com>
Date:   Fri Sep 7 21:30:11 2012 -0400

    Polish async request processing

    This change fixes a cyclical package dependency.

    The change also improves the implementation of
    WebAsyncManager.hasConcurrentResult() following the resolution of
    Apache issue id=53632 and the release of Apache Tomcat 7.0.30 that
    contains the fix.