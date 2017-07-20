commit af921f72495a9de907138b5c410ad3f443977c47
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Wed Dec 10 20:21:44 2014 +1300

    #6622 Logger refactoring: StdErrHandler: had to revert a previous change

    In the tests, logging is disabled. But not when testing the logger itself…