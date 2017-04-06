commit 5572b40b15ed06969c8e0e92866c5afd088484b4
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Sat Sep 13 11:51:30 2014 -0700

    refactor($parse): change 'this' to a $parse keyword instead of scope field

    BREAKING CHANGE:
    - $scope['this'] no longer exits on the $scope object
    - $parse-ed expressions no longer allow chaining 'this' such as this['this'] or $parent['this']
    - 'this' in $parse-ed expressions can no longer be overriden, if a variable named 'this' is put on the scope it must be accessed using this['this']

    Closes #9105