commit 83691a0a36841d5aeda818b58c854d6d8d822056
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sun Jan 6 14:05:55 2013 +0100

    GH-718: Missing structure file causes exception

    A structure file may be absent; in which case a full run is done.
    Due to an error in the refactoring for dynamic exporters was an exception thrown.