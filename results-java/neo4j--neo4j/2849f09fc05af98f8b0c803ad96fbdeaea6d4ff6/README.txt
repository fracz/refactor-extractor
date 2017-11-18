commit 2849f09fc05af98f8b0c803ad96fbdeaea6d4ff6
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Wed Feb 1 19:14:08 2012 +0200

    Added a restart() method on the Broker interface, refactored implementations a bit to make it work with ZooKeeperBroker
    Use Broker#restart() to rebuild internal state when zoo session becomes expired - solves a bug where stale references to a NO_MASTER master client instance would remain around indefinitely.