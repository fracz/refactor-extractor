commit b329943632f40836e1e386451d010283270f9e9b
Author: Shay Banon <kimchy@gmail.com>
Date:   Mon Aug 26 14:29:40 2013 +0200

    improve search while create test
    - improve the test to be more re-creatable
    - have tests for various number of replica counts, to check if failures are caused by searching on replicas that might not have been refreshed yet
    - improve test to test explicit index creation, and index creation caused by index operation
    - have an initial search go to _primary, to check if failure fails when searching on replica because it missed a refresh