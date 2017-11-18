commit 6d6c77a7a9c102f7508e4bc48e0d6eba1fcbc9c6
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Tue Jan 3 19:53:20 2017 -0800

    MINOR: Improvements to Record related classes (part 1)

    Jason recently cleaned things up significantly by consolidating the Message/Record classes
    into the common Java code in the clients module. While reviewing that, I noticed a few things
    that could be improved a little more. To make reviewing easier, there will be multiple PRs.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Ewen Cheslack-Postava <me@ewencp.org>, Jason Gustafson <jason@confluent.io>

    Closes #2271 from ijuma/records-minor-fixes