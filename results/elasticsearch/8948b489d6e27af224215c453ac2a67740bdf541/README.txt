commit 8948b489d6e27af224215c453ac2a67740bdf541
Author: Robert Muir <rmuir@apache.org>
Date:   Tue Jan 6 09:53:54 2015 -0500

    core: Populate metadata.writtenBy for pre 1.3 index files.

        Today this not populated (null) in these cases. But it would be useful to have
        this available, even just for improved error messages.

        The trickiest part today is the handling of 1.2.x files written with
        lucene 4.8+ which have both ES checksums and lucene ones. This is now simplified:
        when the lucene checksum is there, we always use it.

    Closes #9152