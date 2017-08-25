commit 0a37a99b8f856fc6ce9603b97bcd165b70a368d8
Author: Steve Clay <steve@mrclay.org>
Date:   Tue Mar 7 13:19:21 2017 -0500

    chore(js): improves ajax login workflow

    On successful login, the spinner is shown and system messages held until the
    page refreshes. On login failure, the password field is cleared and focused.

    The script no longer fails if `json.forward` is returned as `-1`.

    Ajax/Service is modified slightly to allow setting the input params
    `elgg_fetch_messages` and/or `elgg_fetch_deps` from plugin code.

    Fixes #10774
    Fixes #10805