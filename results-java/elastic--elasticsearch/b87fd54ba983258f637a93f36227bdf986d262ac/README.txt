commit b87fd54ba983258f637a93f36227bdf986d262ac
Author: Ali Beyad <ali@elastic.co>
Date:   Fri Apr 8 23:39:55 2016 -0400

    Improvements to the IndicesService class

    This commit contains the following improvements/fixes:
      1. Renaming method names and variables to better reflect the purpose
    of the method and the semantics of the variable.
      2. For deleting indexes, replace the closed parameter passed to the
    delete index/store methods with obtaining the index's state from the
    IndexSettings that is already passed in.
      3. Added tests to the IndexWithShadowReplicaIT suite, some of which
    show issues in the shadow replica delete process that are captured in
    Github issue 17695.

    Closes #17638