commit 88a2ecc1c6f9c6b9a107e8d82db01572e1c3cce0
Author: Howard Lince III <hlince@live.com>
Date:   Fri Sep 16 14:07:20 2011 -0500

    Validator: Fixed a condition where 'format' wasn't being properly applied to validators with
    multiple format options. It was effectively always 'any' regardless of what it was set to.
        1: Fixing a condition where a null format passed to __callStatic would not be overridden with 'any'
        2: Removed the extract statement for readability.
        3: Fixed $options['all'] being dependent on $format being 'all' as opposed to it's actual
        value of 'any'
        4: Removed superfluous key extraction then rematching for the loop - foreach now just
        iterates over $rules instead of extracting the keys then piecing it back together.
        5: Added a continue statement for elements not in the formats array when $options['all'] is
        false.
        6: Removed format assignment, not necessary.

    ValidatorTest: Added some assertFalse tests for format when applied to validator arrays with
    formats. Previously only assertTrue was run, this led to a problem not being caught where fiormat
     wasn't being applied, any format could be readily swapped and it would still have validated as
     true.