commit acbcfbaf30f73f25df1c45da41132091e7022240
Author: Igor Minar <iiminar@gmail.com>
Date:   Thu Sep 23 05:37:17 2010 +0800

    $cookies service refactoring

    - remove obsolete code in tests
    - add warning logs when maximum cookie limits (as specified via RFC 2965) were reached
    - non-string values will now get dropped
    - after each update $cookies hash will reflect the actual state of browser cookies
      this means that if browser drops some cookies due to cookie overflow, $cookies will reflect that
    - $sessionStore got renamed to $cookieStore to avoid name conflicts with html5's sessionStore