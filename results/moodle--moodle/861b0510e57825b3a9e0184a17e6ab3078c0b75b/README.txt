commit 861b0510e57825b3a9e0184a17e6ab3078c0b75b
Author: Frederic Massart <fred@moodle.com>
Date:   Mon Aug 19 16:17:49 2013 +0800

    MDL-39959: Replace Legacy events - Groups

    This combines the following changes:

    * Event for group member added
    * Event for group member removed
    * Event for group created
    * Event for grouping created
    * Event for group updated
    * Event for grouping updated
    * Event for group deleted
    * Event for grouping deleted
    * Adding tests for deleting functions
    * Bulk remove of members uses low-level API

        The reason for this is that a bulk event has no value from a logging
        perspective as it is not granular. So now, the API is a bit slower,
        but the information the events contain makes sense, beside this is
        not (and should not be) used very often.

        The reason why the events_trigger_legacy() is kept is because we
        cannot create a new event for this, as we don't encourage developers
        to created bulk events, for the reasons mentioned above.

        I removed the call that gets the user record from the function
        groups_remove_member() as it was not required and only appeared
        to check if the user existed. It appears to be safe not to do
        this check as nothing would fail down the line.

    * Bulk unassign of groupings uses low-level API

        As the previous commit, we keep the legacy event for now as it would
        be wrong to create a new event to replace it.

        Also, the code has been changed to call the low-level API to unassign
        groups from groupins, even though at the moment there are no
        events for that function.

    * Bulk deletion of groups uses low-level API

        Again, we keep the legacy event because replacing it would force
        us to create a new event that does not make sense. See MDL-41312.

    * Bulk deleting of groupings uses low-level API
    * Asserting legacy event name in unit tests
    * Minor SQL query and code improvements