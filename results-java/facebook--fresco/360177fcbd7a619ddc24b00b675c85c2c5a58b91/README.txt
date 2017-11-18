commit 360177fcbd7a619ddc24b00b675c85c2c5a58b91
Author: Andrew Jack <me@andrewjack.uk>
Date:   Tue May 17 12:37:50 2016 -0700

    Upgrade to OkHttp3

    Summary:
    Update to [OkHttp](https://github.com/square/okhttp) to [OkHttp3](https://publicobject.com/2015/12/12/com-squareup-okhttp3/)

    We must also update:
    - Fresco to 0.10.0
    - okio to 1.8.0

    **Motivation**
    Reasons for upgrading:
    * Issue #4021
    * "We discovered that RN Android sometimes fails to connect to the latest stable version of NGINX when HTTP/2 is enabled. We aren't seeing errors with other HTTP clients so we think it's specific to RN and OkHttp. Square has fixed several HTTP/2 bugs over the past eight months." - ide
    * OkHttp3 will be maintained & improved, but OkHttp2 will only receive [security fixes](https://publicobject.com/2016/02/11/okhttp-certificate-pinning-vulnerability/)
    * Cleaner APIs - "Get and Set prefixes are avoided"
    * Deprecated/Removed - HttpURLConnection & Apache HTTP
    * React Native apps are currently being forced to bundle two versions of OkHttp (v2 & v3), if another library uses v3
    * Improved WebSocket performance - [CHANGELOG.md](https://github.com/square/okhttp/blob/master
    Closes https://github.com/facebook/react-native/pull/6113

    Reviewed By: andreicoman11, lexs

    Differential Revision: D3292375

    Pulled By: bestander

    fbshipit-source-id: 7c7043eaa2ea63f95854108b401c4066098d67f7