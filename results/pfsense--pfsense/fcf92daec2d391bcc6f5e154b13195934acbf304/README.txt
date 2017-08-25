commit fcf92daec2d391bcc6f5e154b13195934acbf304
Author: Ermal <eri@pfsense.org>
Date:   Tue Nov 16 19:40:20 2010 +0000

    * Use pkg_info -E pkgname* for testing if a package is installed.
    * Do not call eval if we cannot include an .inc file since that will make the whole script fail.
    * Keep the log from the start to the end without overwriting. This makes debugging and problem reporting easy and explains what is done during installation.
    * Check retrun value of download_with_progress_bar to make it possible catching errors during download.
    * Lots of improvements in between

    Related to Ticket #950