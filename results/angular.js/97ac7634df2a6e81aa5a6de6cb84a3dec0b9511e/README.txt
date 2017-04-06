commit 97ac7634df2a6e81aa5a6de6cb84a3dec0b9511e
Author: Strikeskids <github@strikeskids.com>
Date:   Fri Jul 24 15:01:11 2015 -0400

    docs($rootScope.Scope): improve clarity describing $watch with no listener

    The previous explanation in parentheses created a bit of confusion because the documentation stated to leave off the `listener`, but then said "be prepared for multiple calls to your listener". The new explanation clarifies that it is indeed the `watchExpression` that will be executed multiple times.

    Closes #12429