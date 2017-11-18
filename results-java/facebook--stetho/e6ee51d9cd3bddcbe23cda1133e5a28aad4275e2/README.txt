commit e6ee51d9cd3bddcbe23cda1133e5a28aad4275e2
Author: Emil Sjolander <emilsj@fb.com>
Date:   Wed Mar 8 13:15:50 2017 +0000

    Some fixes to recent hit test improvements

    1. Tell chrome to inspect the clicked element, not the view which is
    highlighted.

    2. Find child to return back to front to respect z ordering.

    3. Disregard hidden views and views which are not focusable.