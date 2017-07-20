commit 6125dfeaafb1b677ee68f62c1219702bf3be3b1b
Author: Davert <davert.php@resend.cc>
Date:   Tue Jun 9 05:03:46 2015 +0300

    Added Guzzle 6 connector, moved to Psr7, PhpBrowser to switch connectors

    - added back Guzzle v5 connector
    - fixed sending cookies with Guzzle6 connector
    - improved coverage tests
    - added test for sending cookies

    fixed rm call

    cleanup for phpbrowser test

    fixed webdriver test

    updated composer.lock

    removed usage of Guzzle\Url in ZF connectors