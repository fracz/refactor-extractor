commit 4532cec50d1ebc337e79dd8e67ebb48ec85e6f62
Author: François B <github@bonzon.com>
Date:   Tue Oct 13 22:22:51 2015 +0200

    Improve some comments and minor improvements

    - Define an $error_message and reuse it, instead of recreating the
    error message 3 times
    - Use in_array() check instead of a regex