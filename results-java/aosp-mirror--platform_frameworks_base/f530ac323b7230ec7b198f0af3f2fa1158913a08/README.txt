commit f530ac323b7230ec7b198f0af3f2fa1158913a08
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Jun 21 14:17:48 2012 -0700

    Fix issue #6700897: Activity paused by activating the...

    ...lock screen does not response to onNewIntent()

    We now keep activities stopped even while the lock screen is
    displayed.  (We used to keep them stopped while the screen was
    off, and then resume the top activity when the screen was turned
    on even though they are covered by the lock screen.)

    When a new intent is being delivered to an application, if it
    is not resumed it is held in a pending list until the next
    time the activity is resumed.  Unfortunately that means for
    the case where the activity is being held stopped due to the
    screen off or lock screen, it will not receive any new intents,
    even though it is at the top of the stack.

    Fix this by adding an additional condition that allows the new
    intent to be delivered immediately if the activity manager is
    sleeping and the target activity is at the top of the stack.

    Also some debug output improvements, since pending new intents
    were not being included in the debug output, making it impossible
    to see we were in that situation.

    Change-Id: I5df82ac21657f1c82e05fd8bf21474e883f44e6f