commit 1ebc7959b4d847cf70f59a636eeb05aecf25b24e
Author: johnlenz <johnlenz@google.com>
Date:   Thu Sep 15 12:00:47 2016 -0700

    Avoid repeated lookups of Var when building scopes.  According to GWP data, ~50% of getVar calls are made from declareVar.

    Here we do two things:
    (1) avoid looking vars in the entire scope when the current scope will do
    (2) avoid repeating a lookup when it can be done once

    This can still be improved but this should be significant improvement in the number of hash lookups that are done while building scopes.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=133290311