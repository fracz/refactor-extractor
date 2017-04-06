commit 7c6b288ca2b911dfb76af19c880c95d02247a19e
Merge: 5631002 2c4a43d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Mar 16 13:28:36 2012 +0100

    merged branch rdohms/patch-3 (PR #3614)

    Commits
    -------

    2c4a43d Made option to use symlink explicit in the output. This can clear up any issues for example when running composer update to know if assets:install did a symlink or hard copy.

    Discussion
    ----------

    Made assets:install output copy mode its using

    Made option to use symlink explicit in the output. This can clear up any issues for example when running composer update to know if assets:install did a symlink or hard copy.

    On a general it just makes communication a bit clearer on what is being executed.

    Further improvement is to make Composer install use the same process as was previously used on that server.