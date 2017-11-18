commit fc72ac5736e60ae2ada57f97e7932cdfd3e808bc
Author: Aleksei Statkevich <astatkevich@fb.com>
Date:   Fri Sep 23 12:02:11 2016 -0700

    Periodically cleanup old completed raptor transactions

    Cleanup process will delete records of successful transactions older
    than a specified threshold and records of failed transaction older than
    a specified threshold and not referenced by created shards.

    Note, that an index on 'transactions' table, 'end_time' column is needed
    to improve cleanup query performance.