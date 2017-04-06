commit f308049a90b8e92b062491095cb3ea48e97f9d7e
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Dec 10 18:43:03 2014 +0100

    [ENGINE] Fix updates dynamic settings in InternalEngineHolder

    After the refactoring in #8784 some settings didn't get passed to the
    actual engine and there exists a race if the settings are updated while
    the engine is started such that the actual starting engine doesn't see
    the latest settings. This commit fixes the concurrency issue as well as
    adds tests to ensure the settings are reflected.