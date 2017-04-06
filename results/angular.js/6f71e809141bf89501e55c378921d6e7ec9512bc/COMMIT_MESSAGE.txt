commit 6f71e809141bf89501e55c378921d6e7ec9512bc
Author: Igor Minar <igor@angularjs.org>
Date:   Fri Mar 8 11:43:37 2013 -0800

    fix($route): make nextRoute.$route private

    the `nextRoute` object available in `$routeChangeStart` handler
    accidentaly leaked  property which pointed to the route definition
    currently being matched.

    this was done just for the internal needs of the `$route` implementation
    and was never documented as public api.

    Some confusion arouse around why the $route property was not always
    available on the `nextRoute` object (see #1907). The right thing for us
    to do is to prefix the property with $$ for now and refactor the code
    to remove the property completely in the future. Application developers
    should use the `nextRoute` object itself rather than its `$route` property.
    The main diff is that nextRoute inherits from the object referenced by $route.

    BREAKING CHANGE: in $routeChangeStart event, nextRoute.$route property is gone.

    Use the nextRoute object instead of nextRoute.$route.

    Closes #1907