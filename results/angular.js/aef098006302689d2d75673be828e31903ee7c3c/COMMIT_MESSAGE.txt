commit aef098006302689d2d75673be828e31903ee7c3c
Author: Igor Minar <igor@angularjs.org>
Date:   Thu Jun 13 22:25:00 2013 -0700

    fix($location): default to / for the url base if no base[href]

    With the recent refactoring of $location service we changed this behavior
    resulting in a regression.

    Previously we thought that html5 mode always required base[href]
    to be set in order for urls to resolve properly. It turns out that
    base[href] is problematic because it makes anchor urls (#foo) to
    always resolve to the base url, which is almost always incorrect
    and results in all anchors links and other anchor urls (e.g. svg
    references) to be broken.

    For this reason, we should now start recommending that people just
    deploy to root context (/) and not set the base[href] when using
    the html5 mode (push/pop history state).

    If it's impossible to deploy to the root context then either all
    urls in the app must be absolute or base[href] must be set with the
    caveat that anchor urls in such app won't work.

    Closes #2762