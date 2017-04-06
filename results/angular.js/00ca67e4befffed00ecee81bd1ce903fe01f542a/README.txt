commit 00ca67e4befffed00ecee81bd1ce903fe01f542a
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Sat Oct 30 20:57:13 2010 +0200

    Issue #51: Update extensionMap()

    If user override existing extension, angular properties ($) will be preserved.

    This piece of logic could be refactored into separate method:
    Something like we have extend(), addMissingProperties() - I can't find a name
    for this method...

    Closes #51