commit fbbc6942cf2f4f31d74e5cc35a0812f9174cd0f6
Author: Neil Fuller <nfuller@google.com>
Date:   Wed Mar 5 17:23:51 2014 +0000

    Removing requestLine from HttpURLConnection.getRequestProperties()

    This was breaking an Android test and appears not to be part of the spec for
    HttpURLConnection.

    To fix the test fully another change was required to add additional headers
    passed to the CookieHandler.

    Also added are some tests ported over from Android that test
    CookieHandler/CookieManager and improve coverage of this area.