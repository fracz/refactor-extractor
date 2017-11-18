commit 0175068cab7d294b6cce4369cbd7745dd03198ab
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Fri Jun 5 14:30:09 2015 +0200

    Improve Last-Modified & ETag support

    Prior to this change, the `"Last-Modified"` and "`Etag`" support had
    been improved with SPR-11324: HTTP response headers are now
    automatically added for conditional requests and more.

    This commit fixes the format of the "`Last-Modified`" and "`ETag`"
    values, which were using an epoch timestamp rather than an HTTP-date
    format defined in RFC 7231 section 7.1.1.1.

    Also, Conditional responses are only applied when the given response
    applies, i.e. when it has an compatible HTTP status (2xx).

    Issue: SPR-13090