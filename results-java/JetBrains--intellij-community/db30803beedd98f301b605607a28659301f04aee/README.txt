commit db30803beedd98f301b605607a28659301f04aee
Author: Anna Kozlova <anna.kozlova@jetbrains.com>
Date:   Thu Mar 3 18:24:43 2016 +0100

    inline superclass: don't start push refactoring (with find usages, etc) inside inline; don't collect usages in all inheritors when at the end only one would be processed (IDEA-152480)