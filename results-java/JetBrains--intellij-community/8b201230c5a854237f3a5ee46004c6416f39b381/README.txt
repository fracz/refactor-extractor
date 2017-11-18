commit 8b201230c5a854237f3a5ee46004c6416f39b381
Author: Irina Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Sun Jan 11 18:30:43 2009 +0300

    IDEA-21313 (Svn rollback operation is really slow). really improved; but progress for svn rollback made interminate since currently there's no way to provide real progress (svnkit does not report progress accurately)