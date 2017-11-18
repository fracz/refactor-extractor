commit 508a57d1154f1ec3d0b49bbcf27f7589d55ec8ed
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue May 19 16:32:07 2015 +0400

    refactoring hg command execution process

    * process Listener changed tp CapturingProcessAdapter;
    * string writers changed to String to reuse standard process adapter and to simplify HgCommandResult