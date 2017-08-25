commit 76dc1e2527b7b682e332c84cb2d38e84f47755d2
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Feb 16 12:00:30 2011 +0000

    MDL-26425 tablelib use html_writer when outputting rows and headers.

    This includes refactoring to extract methods like sort_icon and show_hide_link which I think makes the code easier to read.

    Note also the change to make_styles_string that makes it usable with html_writer. (Perhpas we need a css_writer classe ;-))