commit 1c6d0589f1c6429ca84402227cd5954479cf66ed
Author: Victor Chan <victorchan@google.com>
Date:   Sat Jan 9 16:26:37 2016 -0800

    Highlight car nav facet when recent task changes.

    CarStatusBar will now register a ITaskStackListener
    to handle changes in task stack and highlight the
    appropriate facet for the current task. Currently using resource
    defined package names and category to the filter for a given
    facet. OEMs are expected to use categories definied in Intent.java

    Also refactored business logic from CarNavigationBarView
    into CarNavigationBar controller.

    Change-Id: I203917ea43f2f488a1167f27dab84f1c451b3e93