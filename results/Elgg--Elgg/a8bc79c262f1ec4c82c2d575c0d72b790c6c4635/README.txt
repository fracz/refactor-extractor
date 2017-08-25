commit a8bc79c262f1ec4c82c2d575c0d72b790c6c4635
Author: Steve Clay <steve@mrclay.org>
Date:   Tue Jul 28 23:58:01 2015 -0400

    feature(groups): group owner transfer lists users alphabetically

    When you wish to transfer the ownership of a large group it could be
    hard to find the new owner as there was no sorting. Now users are listed
    alphabetically.

    We don’t create full ElggUser objects for improved performance.

    Patch by @jeabakker