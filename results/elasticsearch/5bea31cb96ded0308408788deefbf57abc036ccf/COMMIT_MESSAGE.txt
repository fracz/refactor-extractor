commit 5bea31cb96ded0308408788deefbf57abc036ccf
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Sep 4 17:53:48 2014 +0200

    Internal: refactor copy headers mechanism

    The functionality of copying headers in the REST layer (from REST requests to transport requests) remains the same. Made it a bit nicer by introducing a RestClientFactory component that is a singleton and allows to register useful headers without requiring static methods.

    Plugins just have to inject the RestClientFactory now, and call its `addRelevantHeaders` method that is not static anymore.

    Relates to #6513
    Closes #7594