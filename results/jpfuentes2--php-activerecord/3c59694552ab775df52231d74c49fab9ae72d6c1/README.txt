commit 3c59694552ab775df52231d74c49fab9ae72d6c1
Author: Brian Muse <brian.muse@gmail.com>
Date:   Thu Aug 11 13:20:45 2016 -0400

    Improve eager loading efficiency from `O(n*m)` to `O(m) + O(n*log(m))` (not including the `in_array` improvement)