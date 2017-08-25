commit 63224dee44a31313c4c6017ce87b5070e7f5b40c
Author: Petr Skoda <commits@skodak.org>
Date:   Sun Jun 10 10:26:30 2012 +0200

    MDL-33568 improve DB->count_records*()

    Now always returns integer and invalid queries are detected.