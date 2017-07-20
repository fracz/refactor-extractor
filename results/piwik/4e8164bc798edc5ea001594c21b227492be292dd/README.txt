commit 4e8164bc798edc5ea001594c21b227492be292dd
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Wed Dec 10 15:27:53 2014 +1300

    #6622 Logger refactoring: Piwik FileHandler now extends Monolog's StreamHandler

    We can't remove FileHandler completely because it generates a custom exception message in case of error.