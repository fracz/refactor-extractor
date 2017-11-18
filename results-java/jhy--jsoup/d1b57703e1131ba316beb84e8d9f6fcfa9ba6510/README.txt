commit d1b57703e1131ba316beb84e8d9f6fcfa9ba6510
Author: Jonathan Hedley <jonathan@hedley.net>
Date:   Sun Oct 26 15:50:18 2014 -0700

    Speed improvements

    Android focussed speed improvements.
    Reduced GC load with a flywheel on short strings.
    Optimized core consumeTo methods.