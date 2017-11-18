commit c4d07150eb9cea41b0620ca7aaa23e023fdb0719
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Sep 26 01:28:17 2013 -0400

    Refactored titan-dist and ConsoleSetup

    It seems that executing import and import-static statements in a
    Rexster <init-script> has no effect on client connections.  The log
    shows that the script containing imports was parsed, but the symbols
    still don't exist when connecting to the server with
    rexster-console.sh.  I already know that the <import> and
    <import-static> tags in rexster.xml work for this purpose, so I
    refactored ConsoleSetup.java to generate a properties file containing
    imports = (comma separated symbols...) and staticimports = (comma
    separated symbols...).  This isn't as tidy as using a Groovy
    init-script, but it works.