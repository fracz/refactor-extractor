commit 25225c32ea33ba900f5925c4a232d75ebc7a8da2
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Sep 11 22:42:47 2011 +0200

    Some fixes/refactorings to the daemon. Mainly fixed the expiration problem introduced recently and exposed by the windows build.
    Details:
    - Added handling for the case when daemon attempts to mark daemon as idle/busy/remove and the registry is empty. This is a common case for tests - the test finishes and the cleanup rule removes the temporary folder with the registry file. But the daemon process may still be executing and will attempt to update the registry.
    - When daemon is requested to stop it should not wait for build handlers to finish but simply stop regardless. I removed the handlersExecutor.stop() from the stopping logic.