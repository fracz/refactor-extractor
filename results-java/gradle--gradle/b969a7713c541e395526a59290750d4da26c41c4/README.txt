commit b969a7713c541e395526a59290750d4da26c41c4
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Jul 15 12:30:43 2016 +0200

    Refactor anonymous inner class for better readability

    The double assignment in the constructor was confusing. This commit refactors the code into 2 separate methods that make the intent clearer.

    +review REVIEW-6081