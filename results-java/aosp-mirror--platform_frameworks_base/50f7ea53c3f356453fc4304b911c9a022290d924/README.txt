commit 50f7ea53c3f356453fc4304b911c9a022290d924
Author: jsh <jsh@google.com>
Date:   Tue Sep 15 13:11:25 2009 -0700

    Some SMS logging improvements.

    Use Log.isLoggable() to enable logs at runtime.  Implement SmsResponse.toString()
    so we can see what's returned.

    Hopefully helps with debugging b/2086832.