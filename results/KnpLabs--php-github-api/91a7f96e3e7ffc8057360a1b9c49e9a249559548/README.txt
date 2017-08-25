commit 91a7f96e3e7ffc8057360a1b9c49e9a249559548
Author: Nicolas Pastorino <nicolas.pastorino@ez.no>
Date:   Thu Aug 23 13:02:57 2012 +0200

    Fixed pagination retrieval.

    Per the Github API v3 documentation: "The linebreak is included for
    readability". The actual pagination separator is ",".
    Ref: http://developer.github.com/v3/#pagination