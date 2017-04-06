commit f5d307e426ae75f24f45949bc4da9aa3197a7ae0
Author: Daniel Mitterdorfer <daniel.mitterdorfer@elastic.co>
Date:   Fri Dec 11 16:22:36 2015 +0100

    BulkItemResponse returns status code instead of status name

    In commit fafeb3a, we've refactored REST response handling logic
    and returned HTTP status names instead of HTTP status codes for
    bulk item responses. With this commit we restore the original
    behavior.

    Checked with @bleskes.