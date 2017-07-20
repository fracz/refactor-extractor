commit 5e0fabeb0def6bb2ccc94a0226e8810aedee9ae3
Author: Thomas Rabaix <thomas.rabaix@sonata-project.org>
Date:   Wed Oct 24 13:32:05 2012 +0200

    Add the timezone in the the \DateTime constructor. While the DateTime object
    is initialized without the DateTimeZone instance, the underline library
    execute a stat('.../Europe/Paris') to check if the OS knows the timezone.

    This will improve performance on long run process by avoiding unless i/o calls.