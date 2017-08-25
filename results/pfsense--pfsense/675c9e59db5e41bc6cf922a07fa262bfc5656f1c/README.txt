commit 675c9e59db5e41bc6cf922a07fa262bfc5656f1c
Author: stilez <stilez@users.noreply.github.com>
Date:   Tue Dec 27 01:15:14 2016 +0000

    improve CSS handling for icmp types (overflow/table)

    Minor CSS and formatting improvement to layout for icmp types in rules table. With this change, if several icmp types are specified in the rule, if they won't fit into 2 lines the GUI automatically switches to a scrollable overflow instead, to preserve the table layout. Also underlining is per icmptype so commas etc aren't underlined which is much easier to read