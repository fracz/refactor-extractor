commit 9662302690dbe122c4412e7ee1444397355ebe5b
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Aug 11 04:32:54 2011 +0000

    Refs #2327
     Archive.php improvements
     * Added strong errorhandling, handling sql/php/network errors from the script itself or returned from the http requests
     If there is a critical error during script exec, such as wrong token_auth or mysql shutdown, then the fatal error is throw, PHP error as well, and the script exits directly.
     If there was any non critical errors during execution, the script simply logs errors on screen. Then at the end, it logs them all again on screen for summary then exits (and triggers a PHP error to ensure we trigger cron error handling & email message)
     * Added summary error logs at end of script output + other improvements in the output metrics and messaging
     * Added flags (a different one for days and periods, one per website) to record a website archiving as succesful and not re-trigger the http request when not necessary. Flags are maintained via the piwik_option lookup table.
     * archive.php is now consistently using direct calls to some internal APIs (those that are not processing data) rather than calling over http


    git-svn-id: http://dev.piwik.org/svn/trunk@5095 59fd770c-687e-43c8-a1e3-f5a4ff64c105