commit 5aa913960688d04b75e941acee1e24f42758330f
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Wed Jul 10 23:38:48 2013 +0200

    Fixes case where single HA instance would perpetually try to elect a master

    When an HA instance is alone in the cluster, it will try to contact every
     other instance (i.e. none) for votes and, since by default it holds the master
     role itself, being the only instance, it will wait for the first response
     to come before asking itself. But a response never comes, so the election
     time outs, rinse repeat. This commit fixes that.
    It also introduces a test case that demostrates the problem.
    Finally, it refactors the ElectionContext to not require delegation
     from ElectionState method calls, making mocking easier.