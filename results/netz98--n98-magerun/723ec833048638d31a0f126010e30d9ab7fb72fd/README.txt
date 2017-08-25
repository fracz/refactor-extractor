commit 723ec833048638d31a0f126010e30d9ab7fb72fd
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Tue Jun 30 07:42:31 2015 +0200

    Further improvements on checks

    - Checks show the configuration values so it's more transparent to review a check (cookie-domain and base-url).
    - Duplicate code has been reduced for settings based checks.
    - Introduced if for readability in cookie-domain validation.
    - Some complexity reducement.
    - Fixed typehint.
    - Minor WS formatting.