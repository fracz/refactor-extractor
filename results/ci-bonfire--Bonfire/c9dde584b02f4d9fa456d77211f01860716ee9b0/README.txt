commit c9dde584b02f4d9fa456d77211f01860716ee9b0
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Mon May 20 08:52:15 2013 -0700

    Fix #803 *breaks compatibility of old migrations

    breaks compatibility of old permissions migrations (001-003), so users
    trying to upgrade very old systems may need to pull the older versions
    of those files.

    Replaces the '.' with '_' in the column names in the old version of the
    permissions table.
    003_Permission_system_upgrade reverses the change when creating
    permission names from the column names of the old permissions table, so
    the new (current) permission names retain the '.' previously used in the
    old table's column names.

    Also added 'null' => false to field definitions that did not include an
    entry for whether null is permitted, so we don't make assumptions that
    may be invalid based on the defaults for a particular database.

    Added a variable to hold the role_id for the Administrator user to
    migrations that used it. This should both improve the clarity of the
    code that uses it and make it easier to change the id if necessary.