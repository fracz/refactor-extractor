commit ce7b46e2d2f40b1ca832d95dff54deb236a938ea
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Feb 23 20:23:44 2012 +0100

    Fixed the jetty plugin so that it does not System.exit() the main process any more. This should remove the problem with disapearing daemon. Details:
    -Jetty should never terminate the process; now the thread simply returns when stop was requested.
    -Some refactoring in the Monitor - removed some unnecessary code
    -Improved the test so that it detects the jetty System.exit() problem every time it runs. It also detects it if other executors are used, i.e. not only for the daemon one.