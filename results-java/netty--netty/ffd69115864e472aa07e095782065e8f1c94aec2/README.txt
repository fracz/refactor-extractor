commit ffd69115864e472aa07e095782065e8f1c94aec2
Author: Aron Wieck <aw@crown.de>
Date:   Fri Jul 1 09:07:40 2016 +0200

    Use constant string instead of user provided file name for DiskFileUpload temp file names.

    Motivation:

    DiskFileUpload creates temporary files for storing user uploads containing the user provided file name as part of the temporary file name. While most security problems are prevented by using "new File(userFileName).getName()" a small risk for bugs or security issues remains.

    Modifications:

    Use a constant string as file name and rely on the callers use of File.createTemp to ensure unique disk file names.

    Result:

    A slight security improvement at the cost of a little more obfuscated temp file names.