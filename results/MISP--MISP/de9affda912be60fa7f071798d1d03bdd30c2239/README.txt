commit de9affda912be60fa7f071798d1d03bdd30c2239
Author: iglocska <andras.iklody@gmail.com>
Date:   Wed Aug 6 11:16:27 2014 +0200

    Small performance improvement

    The contributor field in the event view is evaluated based on proposal log entries from the log table affecting the current event. In order to improve performance, the LIKE check for the event ID is moved to the last argument in order to avoid parsing rows that could be ignored by the other arguments quicker.