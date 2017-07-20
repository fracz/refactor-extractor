commit 4622ec44c229144206b2af6e8701d8756bffee73
Author: mark_story <mark@mark-story.com>
Date:   Mon Aug 16 23:33:07 2010 -0400

    Updating the skel and app test.php's to not make a global variable for the dispatcher.  This dramatically improves the performance of the web test runner.