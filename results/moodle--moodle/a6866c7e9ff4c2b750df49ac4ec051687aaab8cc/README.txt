commit a6866c7e9ff4c2b750df49ac4ec051687aaab8cc
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Jun 7 19:17:07 2012 +0100

    MDL-32783 quiz overdue handling: enforce end of grace period

    The summary page was not enforcing the end of the grace period. If the
    user had the summary page open, then they coudl still stubmit after the
    end of the grace period.

    Also, the editing form was not validating that the quiz grace period was
    greater than quiz|graceperiodmin in the quiz configuration, and that
    should be checked, so I added it.

    I fear that processattempt.php is becoming very spaghetti-like with all
    the timing rules, it really needs to be refactored, but not 2 weeks
    before the 2.3 release. (When refactoring, we really need unit tests for
    this.)