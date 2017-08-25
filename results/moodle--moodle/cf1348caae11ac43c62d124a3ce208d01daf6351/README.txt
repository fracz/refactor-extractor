commit cf1348caae11ac43c62d124a3ce208d01daf6351
Author: martinlanghoff <martinlanghoff>
Date:   Wed Sep 12 02:57:26 2007 +0000

    PERF logging - move handling to moodle_request_shutdown()

    By moving the performance profile logging to the very
    end of PHP processing, we cover more pages, notably those
    that don't end up with a footer or a redirect, like file
    serving.

    This should improve quality of our performance logs, and
    help catch some piggies...