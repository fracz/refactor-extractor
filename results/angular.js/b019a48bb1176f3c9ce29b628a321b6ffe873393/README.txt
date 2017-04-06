commit b019a48bb1176f3c9ce29b628a321b6ffe873393
Author: Jeff Cross <middlefloor@gmail.com>
Date:   Thu Jul 25 10:53:43 2013 -0700

    refactor(location): $location now uses urlUtils, not RegEx

    The location service, and other portions of the application,
    were relying on a complicated regular expression to get parts of a URL.
    But there is already a private urlUtils provider,
    which relies on HTMLAnchorElement to provide this information,
    and is suitable for most cases.

    In order to make urlUtils more accessible in the absence of DI,
    its methods were converted to standalone functions available globally.

    The urlUtils.resolve method was renamed urlResolve,
    and was refactored to only take 1 argument, url,
    and not the 2nd "parse" boolean.
    The method now always returns a parsed url.
    All places in code which previously wanted a string instead of a parsed
    url can now get the value from the href property of the returned object.

    Tests were also added to ensure IPv6 addresses were handled correctly.

    Closes #3533
    Closes #2950
    Closes #3249