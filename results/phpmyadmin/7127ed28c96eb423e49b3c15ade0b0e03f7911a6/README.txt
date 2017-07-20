commit 7127ed28c96eb423e49b3c15ade0b0e03f7911a6
Author: Remi Collet <fedora@famillecollet.com>
Date:   Fri Jul 18 08:38:33 2014 +0200

    Fix link in home page.

    Web site: url.php?url=http://www.phpMyAdmin.net/ => OK
    Improve:  url.php?url=http://www.phpmyadmin.neti/home_page/improve.php => broken
    Support:  url.php?url=http://www.phpmyadmin.net/home_page/support.php  => broken

    As domain name are not case sensitive, this seems the simpler fix.
    (other solution, fix index.php to use CamelCase names for all links)

    Signed-off-by: Marc Delisle <marc@infomarc.info>