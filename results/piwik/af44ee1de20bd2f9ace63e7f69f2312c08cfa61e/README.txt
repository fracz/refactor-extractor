commit af44ee1de20bd2f9ace63e7f69f2312c08cfa61e
Author: diosmosis <benaka@piwik.pro>
Date:   Wed Aug 12 21:40:11 2015 +0200

    Make sure purgeOutdatedArchives task is executed in scheduled task run after core:archive run (regression caused by scheduled task execution refactor).