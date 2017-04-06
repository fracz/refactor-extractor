commit a421bd2c27dd77f7f6cb05b040d2024945199365
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Wed Jun 17 10:08:58 2015 +0200

    Avoid duplicate Etag/Last-Modified header values

    This commit improves SPR-13090 and avoids adding duplicate ETag and
    Last-Modified headers in HTTP responses.
    Previously, those were added twice to the response since:

    * we're adding all ResponseEntity headers to the response
    * the `checkNotModified` methods automatically add those headers

    Issue: SPR-13090