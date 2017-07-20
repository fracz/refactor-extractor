commit 49a3a6431fc0638183c6a1f6d36dc48f139ecce7
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Tue Oct 7 16:55:58 2014 +1300

    Refactorings in ScheduledReports to remove useless indentation

    I have refactored the code a bit to make it (hopefully) more readable. I have replaced this:

    ```php
    if (self::manageEvent($reportType)) {
        // do something
    }
    ```

    By this:

    ```php
    if (! self::manageEvent($reportType)) {
        return;
    }

    // do something
    ```

    It's not much but it removes one level of indentation in almost the whole class, which is kind of nice.

    FYI, this is one of the "object calisthenics" rules.