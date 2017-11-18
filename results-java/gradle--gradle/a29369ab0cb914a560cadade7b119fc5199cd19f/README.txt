commit a29369ab0cb914a560cadade7b119fc5199cd19f
Author: Luke Daley <ld@ldaley.com>
Date:   Wed Apr 8 19:06:36 2015 +1000

    Include the location of the output log file in build failure exceptions for native operations.

    This synchronises the experience with other kinds of tasks that produce reports (e.g. JVM test execution) and drastically improves the outputs when there are many failures. The implementation leaves some to be desired, but can be improved in subsequent versions without impacting backwards compatibility.

    +review REVIEW-5406