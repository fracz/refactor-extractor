commit f16695db5ffc0c9eebf98a4f8b794a468528b68c
Author: Ber Clausen <crashcookie@gmail.com>
Date:   Tue Nov 5 20:00:18 2013 -0300

    Move variables to logical blocks.
    Unindent to ease readability, and avoid assigning variables when
    unneeded.
    Free a little memory before entering to recursive intensive loops.