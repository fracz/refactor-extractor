commit c54b3b7738f9e8113b3c0d86229420b076bc8494
Author: diosmosis <benaka@piwik.pro>
Date:   Tue Mar 10 06:11:20 2015 -0700

    More refactoring to Updates.php base & Columns\Updater, make Updates.php methods instance methods, create Update instances via DI, make Columns\Updater use instance methods instead of static, and add integration test for Columns\Updater.