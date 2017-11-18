commit 7ee6bb74109efaf2a1b88ad5faa14a110f377da5
Author: Parag Jain <pjain1@users.noreply.github.com>
Date:   Mon Nov 21 16:29:46 2016 -0600

    option to reset offest automatically in case of OffsetOutOfRangeException (#3678)

    * option to reset offset automatically in case of OffsetOutOfRangeException
    if the next offset is less than the earliest available offset for that partition

    * review comments

    * refactoring

    * refactor

    * review comments