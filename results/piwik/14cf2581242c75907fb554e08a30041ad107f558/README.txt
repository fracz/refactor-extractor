commit 14cf2581242c75907fb554e08a30041ad107f558
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Thu Dec 4 17:34:51 2014 +1300

    #6622 Logger refactoring: move to PSR-3 compatibility

    - keep the log message as string
    - exceptions are logged in context under the "exception" key
    - Piwik\Error objects are replaced by \ErrorException (logged the PSR-3 way)