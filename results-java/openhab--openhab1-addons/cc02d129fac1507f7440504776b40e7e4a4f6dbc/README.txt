commit cc02d129fac1507f7440504776b40e7e4a4f6dbc
Author: John Cocula <john@cocula.com>
Date:   Sun Dec 4 18:03:10 2016 +0000

    [expire] Remove Java 1.8 dependency; add command support, minor refactoring (#4845)

    * [expire] Removed Java 1.8 dependency, small bug fixes, re-added to parent pom.xml

    * formatting

    * formatting

    * Bundle-RequiredExecutionEnvironment must be JavaSE-1.7 on OH1

    * Expire after commands are received as well as after updates; improve log message.

    * Added support for state= and command=; minor refactoring.

    * simplified logic a bit

    * Do less on a polling cycle if possible.

    * Added feature for OH2; cleanup.