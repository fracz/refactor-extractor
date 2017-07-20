commit 2ffa749ea2057bbaeeef59f0e08cc2c288cbbfb9
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Fri Dec 5 11:40:32 2014 +1300

    #6622 Logger refactoring: using Monolog!

    `Piwik\Log` is now a wrapper on top of Monolog. The real logger can now be injected using `Psr\Log\LoggerInterface`.