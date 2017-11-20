commit cd353974bde1761ce8df3f45438b010644ae5e0d
Author: thc202 <thc202@gmail.com>
Date:   Fri Dec 18 19:32:01 2015 +0000

    Fix user selection issues in ContextPanelUsersSelectComboBox

    Change class ContextPanelUsersSelectComboBox to fix the following user
    selection issues:

     - A user is always selected, even if all users are removed from the
     table model (a source of the shown users) it would still show one user
     as selected in the combo box (which means that when used in Forced User
     panel it would be using the user as the Forced User). The fix is to
     change the method setSelectedItem(Object) to check for null selection
     (to set none selected) before checking if the given (null) Object is a
     User (which would always be false). Also, remove redundant null check,
     in a following condition, for the same reason;

     - Rename done to selected user not reflected in the selected user, nor
     is it possible to change that when in the presence of only one user.
     That happens because the modified user is a new User (but still equal).
     The fix is to change the method tableChanged(TableModelEvent) to
     refresh the selected user with the modified user (thus showing the
     latest changes).

    The method tableChanged(TableModelEvent) was also refactored to use all
    event types to do the required selection state checks (for example, when
    a user is added select it if none is yet selected...) and proper
    notification of the changes (for example, call fireIntervalAdded(...)
    when a user is added, fireIntervalRemoved(...) when removed...).
    Another change is to use a (new) method, setSelectedItemImpl(User), that
    sets the selected user without doing any validations, since the state of
    combo box is well known. All internal selection changes are now done
    using that method.

    Fix #2090 - Context: Rename User, can't select in Forced User Panel