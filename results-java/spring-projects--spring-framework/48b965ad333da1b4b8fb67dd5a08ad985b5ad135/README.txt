commit 48b965ad333da1b4b8fb67dd5a08ad985b5ad135
Author: Sam Brannen <sam@sambrannen.com>
Date:   Thu Aug 6 19:42:03 2015 +0200

    Improve performance of NumberUtils

    This commit aims to improve the space and time performance of
    NumberUtils by invoking valueOf() factory methods instead of the
    corresponding constructors when converting a number to a target class.