commit ddb8081982e26cef2d8a4e87a743df6de35332d2
Author: Caitlin Potter <caitpotter88@gmail.com>
Date:   Wed Apr 2 12:05:22 2014 -0400

    refactor(jqLite): make HTML-parsing constructor more robust

    Previously, the jqLite constructor was limited and would be unable to circumvent many of the HTML5
    spec's "allowed content" policies for various nodes. This led to complicated and gross hacks around
    this in the HTML compiler.

    This change refactors these hacks by simplifying them, and placing them in jqLite rather than in
    $compile, in order to better support these things, and simplify code.

    While the new jqLite constructor is still not even close to as robust as jQuery, it should be more
    than suitable enough for the needs of the framework, while adding minimal code.

    Closes #6941
    Closes #6958