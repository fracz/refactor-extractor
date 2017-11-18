commit fa92b77f5a54fbed5df8ef8cdea31415ef93208d
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Wed Feb 10 11:51:04 2016 -0800

    Harmonize znode writing code in RTR and Worker.

    - Throw most exceptions rather than suppressing them, which should help
      detect problems. Continue suppressing exceptions that make sense to
      suppress.
    - Handle payload length checks consistently, and improve error message.
    - Remove unused WorkerCuratorCoordinator.announceTaskAnnouncement method.
    - Max znode length should be int, not long.
    - Add tests.