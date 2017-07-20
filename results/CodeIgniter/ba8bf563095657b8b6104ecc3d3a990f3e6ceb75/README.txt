commit ba8bf563095657b8b6104ecc3d3a990f3e6ceb75
Author: Andrey Andreev <narf@devilix.net>
Date:   Tue Jan 21 19:04:18 2014 +0200

    SQLSRV improvements

    Mainly for performance (issue #2474), but also added a 'scrollable' configuration flag
    and auto-detection for SQLSRV_CURSOR_CLIENT_BUFFERED (only available since SQLSRV 3).