commit 6bc8621f84196c4528b8db6ae157631af7727b07
Author: Phil Davis <phil.davis@inf.org>
Date:   Thu Jun 30 14:00:09 2016 +0930

    Rationalize System Update GUI messages

    At present, when doing a System Update, there is a message box that says:
    "Please wait while the installation of completes.
    This may take several minutes."

    Between "of" and "completes" is the package name, which is blank in the case of a System Update.

    This should fix that issue by defining $pkg_wait_txt appropriately for the $firmwareupdate case.

    Note: It was also easy to refactor out a couple of special "if firmwareupdate" tests, as the code "fell out" nicely by defining the other *_txt variables to suitable strings for the $firmwareupdate case.