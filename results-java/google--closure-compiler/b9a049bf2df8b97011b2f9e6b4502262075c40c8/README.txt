commit b9a049bf2df8b97011b2f9e6b4502262075c40c8
Author: tbreisacher <tbreisacher@google.com>
Date:   Mon May 15 17:24:38 2017 -0700

    Set the language mode correctly after transpiling async functions

    We should also change it from 2016 to 2015 after handling the ** operator, but since that happens in the same pass with transpilation of 2015 features, that isn't really feasible without some more refactoring.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=156126640