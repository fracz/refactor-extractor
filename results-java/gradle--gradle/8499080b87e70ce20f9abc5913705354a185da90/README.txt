commit 8499080b87e70ce20f9abc5913705354a185da90
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Jan 16 15:32:15 2012 +0100

    Copied over an algorithm of finding java executables from ant into our codebase...

    This way we can use that for the daemon to start java using the executable corresponding to the java home provided. The algorightm is copied as-is at the moment. We can refactor it and remove the coupling to ant but that requires a thorough discussion to make sure we don't break something.