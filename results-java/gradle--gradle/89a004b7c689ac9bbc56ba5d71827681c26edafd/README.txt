commit 89a004b7c689ac9bbc56ba5d71827681c26edafd
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Jan 31 12:25:39 2012 +0100

    More improvements to daemon logging diagnostics:
    -the client logs the build request sent to the daemon. This way we can easier cross reference data from daemon log and the client log.
    -the deamon logs it's process id in a way it is visible in the client logs.