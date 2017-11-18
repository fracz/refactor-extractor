commit 71a4d8c0171c6c9814cba0cb7533b52286ee85af
Author: Martin Traverso <martint@fb.com>
Date:   Wed Dec 12 23:34:41 2012 -0800

    Address review feedback #190

    - Implement poison pills for shutting down
    - Attempt recovery in case of abnormal termination of a processor thread
    - Minor refactorings and fixes