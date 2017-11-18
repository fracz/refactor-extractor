commit 508eb90b3c8d494686651bc423838f5750f60c92
Author: Jake Wharton <jw@squareup.com>
Date:   Sun Sep 18 14:32:54 2016 -0400

    Web socket API and implementation improvements.

    * onFailure callback's exception type widened to Throwable. This allows runtime exceptions from other callbacks to be passed along.
    * Ensure the connection is closed properly for all failures.
    * Fix and document the threading inside RealWebSocket. This ensures the listener is always called on the same thread and replies always happen from the correct thread.