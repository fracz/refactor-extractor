commit 7ee041ce6656b85f3ac249201fe11517d71d1673
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Fri Oct 10 10:30:55 2014 -0700

    Improved consistency of database field types #1045

    Set the size of common field types to be consistent between tables.
    Changing all bonfire tables to use the same types and constraints when
    referring to user IDs, email addresses, IP addresses, and module names.
    - The user_id fields in other tables (activities, user_cookies,
    user_meta) should match the definition of users.id. This does not
    address the inability of the primary keys on these tables to keep up
    with the users table.
    - Email address fields should be able to store the largest valid email
    address (254 characters), and the login_attempts table should be able to
    store email addresses in the login field, since the system can use them
    in place of user names.
    - IP address fields should be able to store a value from CI's sessions
    table.
    - Module names should be consistent across tables.
    - Timezones in CI2 can be up to 6 characters, but in CI3 and PHP they
    can be up to 32 characters ('America/Argentina/ComodRivadavia', 40 adds
    a little room for future growth, or in case I missed a longer one).
    Changing the type from char to varchar potentially reduces the storage
    impact of this change on most databases, especially when continuing to
    use CI2 timezones.

    Notes:
    1: It is left to the user to ensure that activities, user_cookies, and
    user_meta do not contain user_id values which are out of range (negative
    values) before performing this migration.

    2: Some databases will complain loudly (or even throw errors and refuse
    to continue) when reducing the size of fields, so uninstalling this
    migration may be difficult.

    3: On some databases the varchar constraint is the number of bytes
    rather than characters, so multi-byte character sets like UTF-8 could
    exceed the constraint before exceeding the maximum valid length of the
    data being stored in that field.

    4: This is not intended to fix cross-database compatibility issues. This
    is simply intended to improve the consistency of the database fields
    within Bonfire. There is still more work to be done in both consistency
    and compatibility.