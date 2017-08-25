commit 1322a56df3a2e8010c442eb3adf1fe4815f6563d
Author: David Mudrak <david@moodle.com>
Date:   Thu Dec 15 21:26:45 2011 +0100

    MDL-30170 MNet peers administration - various usability improvements

    While fixing MDL-30170, the following tiny usability details were
    improved along the way:

    * the maximum length of the peer site title and the hostname is now
      validated (this was actually a real bug as the default value is pre-loaded
      from the peer) so the admin gets a nice validation error instead of
      the database write exception when trying to register a host with a
      longer site title.
    * hard-coded English string 'All Hosts' is now localized
    * the hostname link in the list of MNet peers leads to the peer itself