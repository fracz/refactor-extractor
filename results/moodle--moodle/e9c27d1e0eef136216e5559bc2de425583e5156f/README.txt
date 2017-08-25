commit e9c27d1e0eef136216e5559bc2de425583e5156f
Author: David Mudrák <david@moodle.com>
Date:   Tue Jan 19 17:31:55 2016 +0100

    MDL-52831 users: Automatically strip whitespace from name fields

    This change prevents from registering and/or saving the profile of a
    user with the whitespace instead of the required name.

    Additionally, there is accessibility improvement for better error labels
    in case of missing values (copied over from signup_form).