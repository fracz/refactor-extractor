commit 432f899b29609038be1a4cb8eaad06113cd08750
Author: Sam Brannen <sam@sambrannen.com>
Date:   Sun Jun 16 17:54:55 2013 +0200

    Reduce complexity in ContextLoaderUtilsTests

    This commit refactors ContextLoaderUtilsTests into
    AbstractContextLoaderUtilsTests and several specialized subclasses in
    order to reduce to the growing complexity of ContextLoaderUtilsTests.