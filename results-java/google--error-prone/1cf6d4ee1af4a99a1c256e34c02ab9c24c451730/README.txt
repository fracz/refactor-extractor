commit 1cf6d4ee1af4a99a1c256e34c02ab9c24c451730
Author: Simon Nickerson <sjnickerson@google.com>
Date:   Thu Oct 18 14:32:36 2012 +0100

    Better treatment of negations for the unneeded conditional operator
    refactoring.

    e.g. (x >= y) now gets negated to (x < y) rather than (!(x >= y))