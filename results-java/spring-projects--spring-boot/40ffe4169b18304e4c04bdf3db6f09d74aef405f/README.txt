commit 40ffe4169b18304e4c04bdf3db6f09d74aef405f
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Wed Mar 2 10:51:53 2016 +0000

    Improve DevTools non-embedded in-memory DB shutdown handling

    Shutdown handling has been improved so that it will run after any
    EntityManagerFactory beans have been closed. This ensures that Hibernate
    can, if configured to do so, drop its schema during restart processing.
    Without this change, a benign exception could be logged if the database
    was shutdown before Hibernate.

    Closes gh-5305