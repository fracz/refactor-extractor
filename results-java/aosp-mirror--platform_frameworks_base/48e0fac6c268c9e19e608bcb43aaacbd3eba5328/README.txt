commit 48e0fac6c268c9e19e608bcb43aaacbd3eba5328
Author: James Cook <jamescook@google.com>
Date:   Wed Feb 25 15:44:51 2015 -0800

    Support undo of some programmatic TextView changes

    Applications can use TextWatcher.afterTextChanged() to implement
    post-edit formatting of text, such as inserting spaces in a credit
    card number. From the user's perspective, the insertion of the spaces
    is not a separate action, so that change should be merged with the
    previous undo operation.

    * Force merge undo states for edits that are triggered by callbacks
      after the InputFilter, such as TextWatchers.
    * Reset the undo state when the whole field is reset with setText().
    * Create separate undo operations for direct programmatic changes to
      the Editable (e.g. directly calling insert).
    * Remove part of the non-forced replacement edit merging code. An
      improved version will land in the next CL.

    Bug: 19332904
    Change-Id: Iba5366a5aadbe3534554b668f8d417250deff505