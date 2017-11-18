commit c77f107f5f37c40fff3567722c5d656f81b74c72
Author: Trustin Lee <trustin@gmail.com>
Date:   Sun Jul 8 21:28:56 2012 +0900

    Made the AIO transport faster / Fixed a bug in SingleThreadEventLoopTest

    - Used reflection hack to dispatch the tasks submitted by JDK
      efficiently.  Without hack, there's higher chance of additional
      context switches.
    - Server side performance improved to the expected level.
    - Client side performance issue still under investigation