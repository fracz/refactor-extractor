commit 8f0dcbab804180828d6859b1340c86cf161209fb
Author: Misko Hevery <misko@hevery.com>
Date:   Wed Mar 23 09:33:29 2011 -0700

    feat(scope): new and improved scope implementation

    - Speed improvements (about 4x on flush phase)
    - Memory improvements (uses no function closures)
    - Break $eval into $apply, $dispatch, $flush
    - Introduced $watch and $observe

    Breaks angular.equals() use === instead of ==
    Breaks angular.scope() does not take parent as first argument
    Breaks scope.$watch() takes scope as first argument
    Breaks scope.$set(), scope.$get are removed
    Breaks scope.$config is removed
    Breaks $route.onChange callback has not "this" bounded