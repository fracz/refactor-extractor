commit 444ce1ebba7c9e7b4b13b53179866b1d30fee31e
Author: Todd L. Montgomery <tmont@nard.net>
Date:   Sat Jun 28 13:07:54 2014 -0700

    [Java]: major refactoring of AeronTest. Removed obsoleted SharedDirectories external resource. Removed unnecessary default implementation of newBuffer() in BufferUsageStrategy. Added buffer usage strategy back into Aeron.Context for testing purposes.