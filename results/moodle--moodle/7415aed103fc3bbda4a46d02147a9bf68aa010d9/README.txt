commit 7415aed103fc3bbda4a46d02147a9bf68aa010d9
Author: Petr Skoda <skodak@moodle.org>
Date:   Sun Nov 14 02:01:59 2010 +0000

    MDL-11728 finally defining the exact meaning of is_internal() in auth plugins
    internal means "uses password hash for user authentication", there is a new is_synchronised_with_external() method that indicates if moodle should automatically sync user info with external system after login; I have also improved the default for prevent_local_passwords() which is now defaulting to !is_internal()