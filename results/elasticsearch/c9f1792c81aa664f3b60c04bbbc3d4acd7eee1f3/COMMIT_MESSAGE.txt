commit c9f1792c81aa664f3b60c04bbbc3d4acd7eee1f3
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Apr 29 17:44:32 2014 -0400

    Change default filter cache to 10% and circuit breaker to 60%
    The defaults we have today in our data intensive memory structures don't properly add up to properly protected from potential OOM.
    The circuit breaker, today at 80%, aims at protecting from extensive field data loading. The default threshold today is too permissive and can still cause OOMs.
     The filter cache today is at 20%, and its too high when adding it to other limits we have, reduce it to 10%, which is still a big enough portion of the heap, yet provides improved safety measure.
     closes #5990