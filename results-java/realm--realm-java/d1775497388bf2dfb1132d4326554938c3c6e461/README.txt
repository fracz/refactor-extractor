commit d1775497388bf2dfb1132d4326554938c3c6e461
Author: Chen Mulong <chenmulong@gmail.com>
Date:   Fri Jun 2 19:27:08 2017 +0800

    Fix test case with missing linking object schema

    Those tests pass with current schema implementation since currently it
    will create extra object schemas which are not in the module. This
    behaviour is not right and will fail those tests in the future schema
    refactor.

    So always define all the needed object schemas in the module.