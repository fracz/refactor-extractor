commit 7fc200b212c1d6afbbb44f12087239490d6dfa77
Author: Ketan Padegaonkar <KetanPadegaonkar@gmail.com>
Date:   Fri May 15 14:46:26 2015 +0530

    onsole log coloring improvements and bug fixes (client/browser side)

    See #1092 for a server side implementation written in java.

    * Use a dark background by default, but allow switching to a white
      background
    * Render the console log from the server in using UTF-8 charset
    * The same mechanism that is used to render the live console log
      is used to render the log after the job is complete
    * In order to not blow up the browser from parsing too much text, we
      parse console log in chunks of 1000 lines, in doing so we ensure that
      the color state is not lost between chunks.