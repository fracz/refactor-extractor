commit dafeddca0901956b30059958e0fd4468df44d299
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Wed Nov 6 21:17:12 2013 -0800

    Rework auto-configure report

    Update the auto-configuration report to improve log formatting and to
    separate the internal report data-structure from the JSON friendly
    endpoint data-structure.