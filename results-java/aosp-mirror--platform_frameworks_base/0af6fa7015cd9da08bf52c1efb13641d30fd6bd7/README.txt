commit 0af6fa7015cd9da08bf52c1efb13641d30fd6bd7
Author: Amith Yamasani <yamasani@google.com>
Date:   Sun Jan 17 15:36:19 2016 -0800

    Voice Interaction from within an Activity

    This allows an app to show a voice search button
    and invoke a voice interaction session for use
    within the activity. Once the activity exits, the
    session is stopped.

    Test application has a new activity that
    demonstrates it with the test voice interaction
    service.

    This initial version is functional enough for
    an integration test, with some more tests
    and improvements to come later.

    Bug: 22791070
    Change-Id: Ib1e5bc8cae1fde40570c999b9cf4bb29efe4916d