commit 227dc72078daa5d5249146731c3ab36d980e49a9
Author: Chris Gmyr <cmgmyr@gmail.com>
Date:   Tue Aug 23 16:06:49 2016 -0400

    refactor lists/pluck references so Laravel 5.[0,1,2] installs will work correctly

    now also returning the full Eloquent relationship from threadsWithNewMessages() instead of an array of ids