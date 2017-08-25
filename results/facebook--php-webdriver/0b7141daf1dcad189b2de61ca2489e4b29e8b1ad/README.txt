commit 0b7141daf1dcad189b2de61ca2489e4b29e8b1ad
Author: whhone <whhone@fb.com>
Date:   Mon Jun 2 12:52:59 2014 -0700

    refactor WebDriverCommand

    CommandExecutor should not hold the session id.
    Instead, the session id should be passed inside the command to the
    executor.