commit c2ecfe7b7bffbced652b4da9dcf4ca263d345695
Author: Ariel Weisberg <ariel.wesiberg@datastax.com>
Date:   Fri Apr 3 12:29:17 2015 +0100

    follow up to CASSANDRA-8670:
    providing small improvements to performance of writeUTF; and
    improving safety of DataOutputBuffer when size is known upfront

    patch by ariel and benedict for CASSANDRA-8670