commit dc8325b98d1811f4e8b458e8f4f9660e0bc588de
Author: cushon <cushon@google.com>
Date:   Mon Aug 7 16:26:53 2017 +0200

    Clean up expected exception tests

    in preparation for a change[]

    Previously calls to @CheckReturnValue-annotated methods were ignored in tests
    that used ExpectedException. The enforcement is being changed to only ignore
    calls that immediately follow a call to ExpectedException#expect to improve
    performance.

    Also note that using `assertThrows` or `expectThrows` instead of
    `ExpectedException` is now strongly recommended:
    []

    Tested:
        TAP --sample for global presubmit queue
        []
    PiperOrigin-RevId: 164457920