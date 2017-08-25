commit fd4fdb561d027443406aab554d7ce24619fc0ace
Author: mwhitneysdsu <mwhitney@mail.sdsu.edu>
Date:   Tue Aug 18 08:28:06 2015 -0700

    users/user_meta view PHP 7 compatibility improvements

    Changed instances of `$user->$field['name']` to
    `$user->{$field['name']}`