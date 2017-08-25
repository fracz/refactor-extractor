commit 778b0d920e2926a454c19710fe8028e070c044b2
Author: uwetews <uwe.tews@googlemail.com>
Date:   Fri May 27 22:22:57 2016 +0200

    - bugfix/improvement of compileAlltemplates() follow symlinks in template folder (PHP >= 5.3.1) https://github.com/smarty-php/smarty/issues/224
          clear internal cache and expension handler for each template to avoid possible conflicts https://github.com/smarty-php/smarty/issues/231