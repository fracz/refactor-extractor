commit 195deca6e21faf870f6e69b867264a206d8a6649
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Thu Nov 13 14:51:56 2014 +0000

    test(select): refactor option elements expectations to use `toEqualOption` matcher

    By using a new matcher our tests become less brittle with respect to unimportant
    extra attributes.