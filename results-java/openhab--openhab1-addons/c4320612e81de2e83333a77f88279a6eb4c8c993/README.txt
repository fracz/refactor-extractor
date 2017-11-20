commit c4320612e81de2e83333a77f88279a6eb4c8c993
Author: Bernd Pfrommer <bernd.pfrommer@gmail.com>
Date:   Thu Jan 15 11:29:41 2015 -0500

    InsteonPLM binding improvements and bug fixes, consolidated/sanitized commit:
    - added support for Insteon Hub 2014 (thanks go to Daniel Pfrommer)
    - fixed race condition in writer/reader reply status (Bernd & Rob)
    - added support for power meter (Rob Nielsen)
    - better handling for multiple outstanding queries (Bernd)
    - more detailed debug messages for command and message handlers (Bernd)
    - higher layer support for extracting multiple data fields in single message handler (Bernd)
    - improved suppression of duplicate messages for dimmers (Rob)
    - support for delaying outgoing messages (Bernd & Rob)
    - replaced GreedyDispatcher with SimpleDispatcher (Bernd)