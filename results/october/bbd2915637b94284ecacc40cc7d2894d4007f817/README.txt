commit bbd2915637b94284ecacc40cc7d2894d4007f817
Author: Louis Matthijssen <louis@ict-crew.nl>
Date:   Mon Jun 16 22:46:11 2014 +0200

    Improve the access denied (403) page

    This is an improved version of the current access denied (403) page based on the built-in not found (404) page.

    Shows a link to the CMS backend if the user is logged in and doesn't have permissions for the backend page it's trying to access, but does have permissions for the CMS backend.

    Please note that the following languages are machine translated: ja, pt-br, ru and sv.