commit 5c2669513e8561ce2f62aa08c37ef806e43001d7
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Thu Dec 11 14:37:53 2014 +1300

    #6622 Logger refactoring: Separated error and exception handling from logging

    - the error handler logs warnings and notices, and turns errors into exceptions (`ErrorException`)
    - the exception handler catches all uncatched exceptions and display them to the user (HTML or CLI)
    - the "screen" logging backend has been removed
    - I've normalized exceptions/errors shown to the user in HTML (wether they are catched by the FrontController or not)