commit 49713cebc9102853265324e697513802932c5e2a
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Mon Dec 15 11:24:52 2014 +1300

    #6622 Logger refactoring: restored the "screen" backend to log to HTML notifications

    The "screen" backend (WebNotificationHandler) now logs to HTML notification boxes using the `Notification` api.
    I've re-set the default log level to WARN, and logged PHP notices/warnings to "warning" so that on a default install they are shown in the page in a yellow/warning notification box.

    For the record, the default log level had previously been changed from WARN to ERROR because screen logging was previously messing the HTML/JSON output (which was breaking archiving). With the logger refactoring this is no longer a problem.