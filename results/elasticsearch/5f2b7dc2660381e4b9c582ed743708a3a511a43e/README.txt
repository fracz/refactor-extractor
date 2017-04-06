commit 5f2b7dc2660381e4b9c582ed743708a3a511a43e
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Oct 31 14:23:20 2013 +0100

    improve moving from post_recovery to started
    we need to move to started from post recovery on cluster level changes, we need to make sure we handle a global state change of relocating, which can happen (and not pass through started)