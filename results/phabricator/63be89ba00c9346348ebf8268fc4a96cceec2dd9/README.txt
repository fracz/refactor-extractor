commit 63be89ba00c9346348ebf8268fc4a96cceec2dd9
Author: epriestley <git@epriestley.com>
Date:   Thu Jul 5 16:03:58 2012 -0700

    Improve error message for error 2006

    Summary:
    See discussion here:

    https://secure.phabricator.com/chatlog/channel/%23phabricator/?at=21186

    Basically, MySQL usually raises a good error if we exceed "max_allowed_packet":

      EXCEPTION: (AphrontQueryException) #1153: Got a packet bigger than 'max_allowed_packet' bytes

    But sometimes it gives us a #2006 instead. This is documented, at least:

    >"With some clients, you may also get a Lost connection to MySQL server during query error if the communication packet is too large."

      http://dev.mysql.com/doc/refman//5.5/en/packet-too-large.html

    Try to improve the error message to point at this as a possible explanation.

    Test Plan: Faked an error, had it throw, read exception message. See also chatlog.

    Reviewers: btrahan, skrul

    Reviewed By: skrul

    CC: aran

    Differential Revision: https://secure.phabricator.com/D2923