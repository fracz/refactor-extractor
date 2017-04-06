commit 4eec697c01371e70d0cd5183c8666da4c4c5e618
Merge: def4323 f9f51a5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Mar 23 12:49:19 2012 +0100

    merged branch drak3/process-control (PR #3681)

    Commits
    -------

    f9f51a5 fixed cs
    af65673 [Process] Added support for non-blocking process control Added methods to control long running processes to the Process class:  - A non blocking start method to startup a process and return    immediately  - A blocking waitForTermination method to wait for the processes    termination  - A stop method to stop a process started with start All status-getters like getOutput were changed to return real-time data

    Discussion
    ----------

    [Process] Added support for non-blocking process control

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes [![Build Status](https://secure.travis-ci.org/drak3/symfony.png?branch=process-control)](http://travis-ci.org/drak3/symfony)
    Fixes the following tickets: #1049
    Todo: -

    Added methods to control long running processes (as described in issue #1049) to the Process class:
     - A non blocking start method to startup a process and return
       immediately
     - A blocking waitForTermination method to wait for the processes
       termination
     - A stop method to stop a process started with start
    All status-getters like getOutput were changed to return real-time data

    ---------------------------------------------------------------------------

    by Seldaek at 2012-03-23T10:52:30Z

    Overall this seems like a good improvement. I didn't check the code in detail though.

    ---------------------------------------------------------------------------

    by drak3 at 2012-03-23T11:21:45Z

    @stof @Seldaek thanks, fixed