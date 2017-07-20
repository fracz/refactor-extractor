commit a140a849036fe59bd1817edcf7216e6aaf330efd
Author: robocoder <anthon.pang@gmail.com>
Date:   Fri Feb 18 16:28:04 2011 +0000

    refs #409:
     * always use Crockford's JSON module (renamed to JSON2) to workaround broken "native implementations"
     * add JSON unit tests
     * revert [3893] and [3900]; rewrite getVisitorId() per comment:80
     * refactor browser feature detection for fingerprinting (used to generate uuid)
     * setDomains() now takes either '*.domain' or '.domain'
     * Safari emits warnings for Content-Length and Connection as "unsafe headers" in XHR POST request

    refs #1984:
     * partially revert [3882] in order for the unit tests to run
     * fix inconsistency in getCustomVariable() depending on whether it is loaded from memory or from a cookie

    refs #2078 Webkit bug ("Failed to load resource") when link target is the current window/tab
     * requires further discussion because the workaround may not be desirable behavior, i.e.,
    {{{
    if ((new RegExp('WebKit')).test(navigatorAlias.userAgent)
        && (!sourceElement.target.length || sourceElement.target === '_self')
        && linkType === 'link')
    {
        // open outlink in a new window
        sourceElement.target = '_blank';
    }
    }}}


    git-svn-id: http://dev.piwik.org/svn/trunk@3939 59fd770c-687e-43c8-a1e3-f5a4ff64c105