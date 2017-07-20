commit 2cd77b5b5817fff8d4be36e39d050b0f42a7dd99
Author: epriestley <git@epriestley.com>
Date:   Mon Feb 16 11:30:49 2015 -0800

    Improve taskmaster behavior on empty queues

    Summary:
    Right now, taskmasters on empty queues sleep for 30 seconds. With a default setup (4 taskmasters), this averages out to 7.5 seconds between the time you do anything that queues something and the time that the taskmasters start work on it.

    On instances, which currently launch a smaller number of taskmasters, this wait is even longer.

    Instead, sleep for the number of seconds that there are taskmasters, with a random offset. This makes the average wait to start a task from an empty queue 1 second, and the average maximum load of an empty queue also one query per second.

    On loaded instances this doesn't matter, but this should dramatically improve behavior for less-loaded instances without any real tradeoffs.

    Test Plan: Started several taskmasters, saw them jitter out of sync and then use short sleeps to give an empty queue about a 1s delay.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D11772