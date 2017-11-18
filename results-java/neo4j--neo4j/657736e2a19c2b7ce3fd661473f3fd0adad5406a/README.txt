commit 657736e2a19c2b7ce3fd661473f3fd0adad5406a
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Tue Mar 14 14:23:39 2017 +0100

    align and improve heartbeat response handling

    The heartbeat responses were not handled using the state/outcome pattern
    used for all other core logic, this has been addressed.

    The election timer is now also reset when becoming a leader and
    heartbeats are immediately sent out as well.