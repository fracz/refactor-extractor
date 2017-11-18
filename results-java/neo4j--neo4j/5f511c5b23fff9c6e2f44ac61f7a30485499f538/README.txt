commit 5f511c5b23fff9c6e2f44ac61f7a30485499f538
Author: Andrew Kerr <andrew.kerr@neotechnology.com>
Date:   Mon Oct 9 10:52:47 2017 +0100

    Raft message log line rewrite to aid machine readability.

    Include date, both to and from members, type of message rather than class of message, arranged as space separated fields and with message quoted.