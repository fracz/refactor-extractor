commit c30371168f1c678d104e57aab20639248435c5d7
Author: Damyon Wiese <damyon@moodle.com>
Date:   Wed Jun 12 14:10:55 2013 +0800

    MDL-37459 admin: Code improvements for admin defaults.

    Split apply_admin_defaults_after_data so the defaults are set before
    set_data is called.

    Change function from private to protected.

    Change check for if module for is an add or an update.