commit 4d6b8e50cbc4ab844f0862a8854b06e497c2edf4
Author: Adam Murdoch <adam.murdoch@gradleware.com>
Date:   Fri Aug 29 13:43:39 2014 +1000

    GCC can build x64 binaries on Windows in general, so remove this check. Still need to improve detection of whether what we find in the path can build x86 or x64 binaries or both or neither, but this is true of Linux and OS X too.