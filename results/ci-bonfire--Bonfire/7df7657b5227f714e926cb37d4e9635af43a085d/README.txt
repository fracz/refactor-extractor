commit 7df7657b5227f714e926cb37d4e9635af43a085d
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Mon Apr 21 07:37:34 2014 -0700

    Profiler cleanup (readability, JS, CSS)

    - Moved values of style attributes into the CSS (in the style element)
    - Reduced the number of font-family definitions in the CSS by making the
    rule for the monospace font more specific
    - Changed the appearance of the heading within each profiler panel to
    improve readability (white on panel's color rather than panel's color on
    black)
    - Added some padding and the hover coloration to the active menu item to
    give it a more tab-like appearance and make the active menu item more
    obvious
    - Fixed the JavaScript to add/remove the 'current' class on the active
    menu item