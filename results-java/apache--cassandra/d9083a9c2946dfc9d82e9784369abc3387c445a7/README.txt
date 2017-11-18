commit d9083a9c2946dfc9d82e9784369abc3387c445a7
Author: Ariel Weisberg <ariel.weisberg@datastax.com>
Date:   Wed Apr 6 15:26:37 2016 -0400

    Use a CAS loop in UUIDGen instead of a sychronized block to improve performance under contention

    patch by Ariel Weisberg; reviewed by Joel Knighton for CASSANDRA-11517