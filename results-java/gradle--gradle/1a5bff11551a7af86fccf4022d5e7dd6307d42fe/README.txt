commit 1a5bff11551a7af86fccf4022d5e7dd6307d42fe
Author: Jendrik Johannes <jendrik@gradle.com>
Date:   Wed Sep 28 17:32:43 2016 +0200

    AbstractCopyTask.rename() should not rename or fail for null

    This is how it is specified: 'The closure may return null, in which
    case the original name will be used.' The implementation now follows
    that.

    I added a test for the null case and refactored the existing test
    for the non-null case from JMock to Spock.

    +review REVIEW-6270