commit f72b7c22bb2cbafca96d7c52c7e15d25af9cb1f3
Author: robocoder <anthon.pang@gmail.com>
Date:   Wed Jan 19 20:52:00 2011 +0000

    refs #409 - first party cookies

     * API changes:
       * added: setCookieNamePrefix(cookieNamePrefix)
       * added: setCookieDomain(domain)
       * added: setCookiePath(path)
       * added: setVisitorCookieTimeout(timeout) - defaults to 2 years since last page view
       * added: setSessionCookieTimeout(timeout) - defaults to 30 minutes since last activity
       * added: setReferralCookieTimeout(timeout) - defaults to 6 months from the first visit
       * added: setConversionAttributionFirstReferer(enable)
       * added: getVisitorId()
         * for asynchronous tracking, use:
    {{{
            var visitorId;

            _paq.push(function () {
                    visitorId = this.getVisitorId();
            });
    }}}

     * Cookie notes:
       * The default cookie path is '/'.  This might be viewed as a potentially insecure default because it allows cookies to be shared across directories on the same domain.  (Again, see the social network example.)  This is unfortunately, a necessity.  If we leave the path blank, the behaviour is undefined (i.e., browser or browser-version dependent).  For example, earlier versions of Firefox would default to '/'; later versions default to the origin path.
       * I was hoping to avoid this, but I added a hash to the cookie content similar to GA's setAllowHash().  This is needed for two reasons:
         1. Cookies are uniquely identified by the tuple (key,domain,path).  Hashing only the domain is a bug.  (See "social network website" use case.)
         2. There's a long-standing cookie+subdomain bug in Firefox (Gecko) dating back to 1.0 that leaks cookies from "example.com" (not ".example.com") to "xyz.example.com".  @see https://bugzilla.mozilla.org/show_bug.cgi?id=363872
     * changed internal setCookie() method to take expiry time in milliseconds (was days)
     * removed internal dropCookie() method as it was never used

     @todo Missing unit tests and cross browser testing

    refs #739 - piwik.js improvements

     * jslint 2011-01-09
     * new unit tests (integrated jslint, is_a functions, sha1(), utf8_encode(), etc)
     * use ECMAScript String.substring() instead of non-standard (although widely supported) String.substr()
     * implement domainFixup() so "example.com" and "example.com." are equivalent

     * API changes:
       * added: killFrame() - a frame buster
       * added: redirectFile( url ) - redirect if browsing off-line, aka file: buster; url is where to redirect to
       * added: setHeartBeatTimer( delay ) - send heart beat 'delay' milliseconds after initial trackPageView(); set to 0 to disable
       * removed: piwik_log() - legacy tracking code; see trackLink()
       * removed: piwik_track() - legacy tracking code; see trackPageView()
       * removed: setDownloadClass() - deprecated; see setDownloadClasses()
       * removed: setLinkClass() - deprecated; see setLinkClasses()

    refs #752 - track middle mouse button clicks (via mousedown+mouseup pseudo-click handler); defaults to tracking true "clicks"

     * API changes:
       * modified: addListener( element, enablePseudoClickHandler = false )
       * modified: enableLinkTracking( enablePseudoClickHandler = false )

    refs #1984 - custom variables vs custom data

     @todo These are just stubs.

     * API changes:
       * added: setCustomVar(slotId, key, value, opt_scope) - scope is 1 (visitor), 2 (sesson), 3 (page)
       * added: getCustomVar(slotId)
       * added: deleteCustomVar(slotId)

     * API changes for consistency:
       * added: setCustomVar(slotId, obj, opt_scope)
       * added: setCustomData(key, value)
       * for the equivalent of deleteCustomData(), use:
    {{{
        tracker.setCustomData(null);
    }}}



    git-svn-id: http://dev.piwik.org/svn/trunk@3783 59fd770c-687e-43c8-a1e3-f5a4ff64c105