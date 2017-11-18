commit 48979ab6c88ada1f9992771b615b560b377f0fd3
Author: kimchy <kimchy@gmail.com>
Date:   Tue Jun 15 10:27:26 2010 +0300

    improve gateway recovery when using delay index creation, close the loophole when the master was shut down before the delay expired and now other node becoming master will do the recovery