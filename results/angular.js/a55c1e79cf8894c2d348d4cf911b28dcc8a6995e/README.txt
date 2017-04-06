commit a55c1e79cf8894c2d348d4cf911b28dcc8a6995e
Author: Karl Seamon <bannanafiend@gmail.com>
Date:   Thu Dec 5 18:56:52 2013 -0500

    chore($resource): Use shallow copy instead of angular.copy

    Replace calls to angular.copy with calls to a new function, shallowClearAndCopy.
    Add calls to copy for cache access in $http in order to prevent modification of cached data.
    Results in a measurable improvement to the startup time of complex apps within Google.

    Closes #5300