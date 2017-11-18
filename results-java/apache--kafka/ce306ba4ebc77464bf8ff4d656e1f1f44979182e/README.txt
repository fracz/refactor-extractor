commit ce306ba4ebc77464bf8ff4d656e1f1f44979182e
Author: flavio junqueira <fpj@apache.org>
Date:   Sun Oct 18 15:23:52 2015 -0700

    KAFKA-2639: Refactoring of ZkUtils

    I've split the work of KAFKA-1695 because this refactoring touches a large number of files. Most of the changes are trivial, but I feel it will be easier to review this way.

    This pull request includes the one Parth-Brahmbhatt started to address KAFKA-1695.

    Author: flavio junqueira <fpj@apache.org>
    Author: Flavio Junqueira <fpj@apache.org>

    Reviewers: Ismael Juma <ismael@juma.me.uk>, Jun Rao <junrao@gmail.com>

    Closes #303 from fpj/KAFKA-2639