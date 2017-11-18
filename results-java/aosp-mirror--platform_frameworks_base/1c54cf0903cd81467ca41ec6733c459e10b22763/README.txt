commit 1c54cf0903cd81467ca41ec6733c459e10b22763
Author: Karl Rosaen <krosaen@android.com>
Date:   Thu Jun 4 15:58:36 2009 +0100

    Detect impressions, and cleanup the SearchDialog / SuggestionCursor communication.

    (framework portion)

    There are now 4 times the search dialog will check with the cursor:
    - after data set changed
    - when an item is clicked
    - when the cursor is about to be closed
    - when an item at a particular position is detected as showing

    these are now the points where we can add data in either direction, which we use to accomplish:
    - finding out whether there are any pending results
    - find out if there is a position at which to notify when it is displayed (the "more results" triggering)
    - toggling the "more results" button
    - sending the max position that was displayed when the cursor is done

    the new behavior (in addition to the refactoring) is improved detection of "more results" to trigger the additional sources
    (it is now precise), and detection of which items were displayed to the user.