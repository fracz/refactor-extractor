commit 83816db09a45ea3bcdaac23e0ec6a5c476e63dae
Author: rvesse <rvesse@dotnetrdf.org>
Date:   Tue Mar 3 09:49:47 2015 +0000

    Fix bug with UIWebServer getting the wrong conf (TACHYON-286)

    After the configuration changes I found that tests were hanging in my
    environment and looking at the logs the web server was failing to start
    properly because it wasn't respecting the test configuration that
    LocalTachyonMaster created.

    The problem was that UIWebServer simply creates a new TachyonConf
    instance and thus gets the default values rather than it being passed
    the TachyonConf object by the TachyonMaster to which it belongs.  This
    commit simply changes the behaviour to do that.

    It also improves the logging when the web server starts under a test
    environment (port = 0) so it actually prints the port chosen for the web
    server.