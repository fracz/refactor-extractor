commit 50ed196e514405afe2cce789ae732fdde31caf2c
Author: Petr Skoda <commits@skodak.org>
Date:   Sat Oct 22 14:28:18 2011 +0200

    MDL-29670 cron cleanup and minor fixing

    Auth and enrol is moved to the top because other plugin types depend on accurate user list and enrolments. Passwords and failed login messages are generated during every cron execution.  Contexts are build properly in each execution. Fixed deleting of unconfirmed users. And minor phpdocs and comments improvements.