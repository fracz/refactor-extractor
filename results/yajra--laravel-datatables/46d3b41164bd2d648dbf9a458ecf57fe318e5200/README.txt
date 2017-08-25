commit 46d3b41164bd2d648dbf9a458ecf57fe318e5200
Author: Ansient <criosphere@gmail.com>
Date:   Wed Feb 17 15:51:21 2016 +0100

    In the refactoring process, of the column search system, an error related to case-sensitivity check led to a wrong interpretation and therefore to an incorrect behaviour of the logic.

    If the isCaseInsensitive function is true, the compileColumnSearch function is called with the flag caseSensitive set to true, clearly wrong.
    The same issue occurs if the isCaseInsensitive function is false.

    Now the boolean flag caseSensitive is set correctly for both if-else blocks.