commit 28a07a8d13a05f923b3d231283261e484078de72
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Fri Dec 5 11:01:47 2014 +1300

    #6622 Logger refactoring: removed manual overriding of the log level in CLI since it's now dependent of the verbosity level

    `Piwik\Log::setLogLevel` is now deprecated and doesn't do anything. Its usages have been removed.