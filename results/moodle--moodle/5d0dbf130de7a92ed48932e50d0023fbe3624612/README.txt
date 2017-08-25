commit 5d0dbf130de7a92ed48932e50d0023fbe3624612
Author: Penny Leach <penny@liip.ch>
Date:   Wed Nov 18 12:27:15 2009 +0000

    portfolio MDL-20872 elegant handling of cleanup/display race condition

    lots of work to elegantly resolve the issue between interactive browser
    sessions and "pull" portfolio plugins that cause a race condition where we lose
    the ability to display information to the user if the pull cleans up the export
    first.  this also improves the portfolio transfer log display for later