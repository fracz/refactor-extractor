commit 540ea28c96ad67692df4b59d921225c2aec35f63
Author: Irina Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Sun Jan 11 18:30:41 2009 +0300

    IDEA-21313 (Svn rollback operation is really slow). really improved; but progress for svn rollback made interminate since currently there's no way to provide real progress (svnkit does not report progress accurately)