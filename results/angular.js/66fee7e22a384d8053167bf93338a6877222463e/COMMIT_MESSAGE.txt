commit 66fee7e22a384d8053167bf93338a6877222463e
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Sat Aug 8 21:37:46 2015 -0700

    refactor($compile): move $scope.$on('$destroy') handler out of initializeDirectiveBindings

    Since only one of three invocations of `initializeDirectiveBindings` actually
    adds a `$destroy` handler to the scope (the others just manually call unwatch
    as needed), we can move that code out of this method.

    This also has the benefit of simplifying what parameters need to be passed
    through to the linking functions

    Closes #12528