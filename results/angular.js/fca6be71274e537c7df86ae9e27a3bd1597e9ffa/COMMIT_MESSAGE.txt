commit fca6be71274e537c7df86ae9e27a3bd1597e9ffa
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Tue Sep 9 22:03:10 2014 -0700

    perf($parse): execute watched expressions only when the inputs change

    With this change, expressions like "firstName + ' ' + lastName | uppercase"
    will be analyzed and only the inputs for the expression will be watched
    (in this case "firstName" and "lastName"). Only when at least one of the inputs
    change, the expression will be evaluated.

    This change speeds up simple expressions like `firstName | noop` by ~15%
    and more complex expressions like `startDate | date` by ~2500%.

    BREAKING CHANGE: all filters are assumed to be stateless functions

    Previously it was a good practice to make all filters stateless, but now
    it's a requirement in order for the model change-observation to pick up
    all changes.

    If an existing filter is statefull, it can be flagged as such but keep in
    mind that this will result in a significant performance-penalty (or rather
    lost opportunity to benefit from a major perf improvement) that will
    affect the $digest duration.

    To flag a filter as stateful do the following:

    myApp.filter('myFilter', function() {
      function myFilter(input) { ... };
      myFilter.$stateful = true;
      return myFilter;
    });

    Closes #9006
    Closes #9082