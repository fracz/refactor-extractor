commit 40c761e01afa8ab3dd1df54dd3093529db81c698
Author: Dan Poltawski <dan@moodle.com>
Date:   Mon Feb 3 12:42:35 2014 +0800

    MDL-42882 upgrade: improved SQL query

    Kudos to Tim Hunt who came up with this, on mysql with 4 milion
    records (and a fast SSD) the performance difference is:

    Original: 36.83 sec
    New query: 8.63 sec