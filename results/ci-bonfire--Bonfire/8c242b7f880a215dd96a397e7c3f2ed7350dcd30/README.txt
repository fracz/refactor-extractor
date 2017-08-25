commit 8c242b7f880a215dd96a397e7c3f2ed7350dcd30
Author: Alan Jenkins <alan.christopher.jenkins@gmail.com>
Date:   Sun Jul 29 17:11:36 2012 +0100

    activities report: fix form controls with no associated label

     - mark up the label text for <select> by wrapping both together
       in a <label>

     - bootstrap <select> defaults to block, so this change would alter the
       design (for the worse).  Use .form-inline to keep everything on
       one line.  Doing this the right way pays dividends: as a side effect,
       this improves the vertical alignment of the <select> with the label
       text.