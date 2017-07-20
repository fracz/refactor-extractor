commit 598ce5b5988d53a6d0405c6b346c2d795f00285a
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Wed Dec 10 17:05:41 2014 +1300

    #6622 Logger refactoring: Turned the Exception (text) formatter into a processor + added tests

    Monolog handlers support 1 formatter. Using processors instead of nested formatters leads to much simpler code.