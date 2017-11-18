commit f1710e6a9b46ed5ec6f2ee8829af9fb200026b0e
Author: Kaloyan Raev <kaloyan-raev@users.noreply.github.com>
Date:   Wed Nov 30 16:17:14 2016 +0200

    Performance improvements and other fixes in code completion (#3146)

    * Avoid overloading the DOM tree of the ContentAssistWidget

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>

    * Ensure the code assist widget is closed when applying proposal

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>

    * Respect isIncomplete flag in the completion result

    If the isIncomplete flag is false, i.e. the completion result is
    complete, then additional typing for the same word should not trigger a
    new completion request. The latest completion result should be reused.

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>

    * Fix flickering between keystrokes during code completion

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>

    * Fine tune the rules for making new completion request

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>

    * Set the correct offset when applying code completion

    * Force a completion request message on Ctrl+Space

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>

    * Fixed retrieval of CompletionItem document via Resolve request

    Signed-off-by: Kaloyan Raev <kaloyan.r@zend.com>