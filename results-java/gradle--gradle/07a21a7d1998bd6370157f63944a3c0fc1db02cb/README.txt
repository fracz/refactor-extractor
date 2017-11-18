commit 07a21a7d1998bd6370157f63944a3c0fc1db02cb
Author: Adam Murdoch <adam@gradle.com>
Date:   Wed Mar 9 12:35:38 2016 +1100

    Added some throttling to the rendering of log events to the console.

    This basically means that progress log events that are received for very short operations, or for very frequent status updates, will be coalesced into fewer updates or potentially discarded when short enough. This change improves build times for those users with slow consoles (and not-so-slow consoles).

    The throttling applies only when generating rich console output, either at the command-line or through the tooling API.