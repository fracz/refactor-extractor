commit 221b74d38a006b0cede71423bbc1d98d82066539
Author: Timo Besenreuther <timo.besenreuther@gmail.com>
Date:   Mon Apr 8 16:31:44 2013 +0200

    refs #3158 row evolution

     * bug fix: the export beneath row evolution graphs didn't work. the reason was that it passed an expanded=1 and a label parameter to the API. solution: ignore expanded parameter if label parameter is set.
     * small code improvements