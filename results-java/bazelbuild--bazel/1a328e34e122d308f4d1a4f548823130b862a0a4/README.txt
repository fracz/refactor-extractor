commit 1a328e34e122d308f4d1a4f548823130b862a0a4
Author: ulfjack <ulfjack@google.com>
Date:   Thu Apr 6 10:25:16 2017 +0000

    Explicitly document the state transition on MetadataHandler

    The ActionMetadataHandler does an explicit state transition on
    discardOutputMetadata. Before the call, it may be used for action cache
    checking, and after the call it may be updated with execution results.

    Several of the methods now throw if they're used incorrectly, so I had to
    refactor the control flow in ActionExecutionFunction to correctly call
    discardOutputMetadata on the MetadataHandler in all cases. I discovered a
    resource leak (of FileOutErr) in IncludeParseFunction while I was at it, so
    I plugged that as well.

    One step towards #1525.

    PiperOrigin-RevId: 152363982