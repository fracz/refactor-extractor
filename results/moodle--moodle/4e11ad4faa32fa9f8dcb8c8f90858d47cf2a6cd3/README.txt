commit 4e11ad4faa32fa9f8dcb8c8f90858d47cf2a6cd3
Author: moodler <moodler>
Date:   Sun Aug 15 07:27:52 2004 +0000

    Added a new "auth" field to the user table.  This field contains the
    authentication mechanism used to create that user record.

    Also added code to upgrade existing systems to have entries in that
    field, and for new users to also have that field defined.

    This will allow us to later improve the login procedure to be able to
    handle various types of authentication.