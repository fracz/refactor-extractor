commit fcd2a8131a3cb3e59a616bf31e61510b5c3a97d3
Author: Karl Seamon <bannanafiend@gmail.com>
Date:   Thu Dec 5 18:56:52 2013 -0500

    perf($resource): use shallow copy instead of angular.copy

    Replace calls to angular.copy with calls to a new function, shallowClearAndCopy.
    Add calls to copy for cache access in $http in order to prevent modification of cached data.
    Results in a measurable improvement to the startup time of complex apps within Google.

    Closes #5300