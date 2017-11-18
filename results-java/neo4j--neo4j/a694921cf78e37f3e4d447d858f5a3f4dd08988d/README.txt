commit a694921cf78e37f3e4d447d858f5a3f4dd08988d
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Wed Apr 24 12:36:43 2013 +0200

    Performance improvements

    o Don't do clever reflective stuff in CompositeStatementContext
    o Don't do the double-conversion of property id -> property key in the kernel API
    o CleanupService now schedules itself using a timer
    o Added a temporary performance test to help drive performance improvements in 2.0