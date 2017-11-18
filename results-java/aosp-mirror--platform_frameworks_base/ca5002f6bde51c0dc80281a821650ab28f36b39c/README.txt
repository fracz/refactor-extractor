commit ca5002f6bde51c0dc80281a821650ab28f36b39c
Author: Felipe Leme <felipeal@google.com>
Date:   Wed Aug 3 11:51:00 2016 -0700

    Fixed BugreportReceiverTest failures:

    - testProgress_changeJustDetailsTouchingNotification was failing because
      the notification mechanism changed and now provides a way to expand
      and collapse the actions bar, and the test was collapsing it instead
      of opening the details dialog. It was fixed by tapping the
      notification content instead of the notification title.
    - Similarly, openProgressNotification() was refactored to use the
      bugreport name instead of id.
    - Uses getObject() (instead of getVisibleObject()) to get the activity
      from the intent chooser.
    - Removed the redundant call to back to dismiss the keyboard, which was
      causing some tests to fail due to an accessibility bug.
    - Retry a few times in case the bugreport name system property assertion
      fails, since the property is set by a background thread.
    - Improved how notifications are canceled.
    - Created a sleep() helper.

    Besides the test case improvements, it also removed a redundant call to
    setCancelable() in the main code.

    BUG: 30641229
    BUG: 30639703

    Change-Id: Icd79fada22d0b8e4be034068c3e9143ef0134eed