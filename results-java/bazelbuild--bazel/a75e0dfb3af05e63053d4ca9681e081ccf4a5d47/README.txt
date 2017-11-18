commit a75e0dfb3af05e63053d4ca9681e081ccf4a5d47
Author: philwo <philwo@google.com>
Date:   Fri Apr 21 15:53:29 2017 +0200

    No longer print a warning when a sandbox directory couldn't be deleted.

    With the process-wrapper improvements and the additional deletion of the sandbox base in the SandboxModule in, this should be reliable enough. The warning was also not actionable for users and annoyed them, so let's get rid of it.

    PiperOrigin-RevId: 153823045