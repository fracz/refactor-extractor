commit 17e6195fd605be8f34d3af857aeb6c7d0ffea697
Author: Ryan Ernst <ryan@iernst.net>
Date:   Thu Dec 3 15:20:41 2015 -0800

    Build: Fix updateShas to not barf on disabled license checks and even compile correctly

    These were just results of not testing properly after refactoring.

    closes #15224