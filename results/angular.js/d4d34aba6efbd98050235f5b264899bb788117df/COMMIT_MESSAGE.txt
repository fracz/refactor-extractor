commit d4d34aba6efbd98050235f5b264899bb788117df
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Aug 12 10:34:01 2013 -0700

    fix($location): don't initialize hash url unnecessarily

    After a recent refactoring using $location in the default hashbang mode would result
    in hash url being initialized unnecessarily in cases when the base url didn't end
    with a slash.

    for example http://localhost:8000/temp.html would get rewritten as
    http://location:8000/temp.html#/temp.html by error.