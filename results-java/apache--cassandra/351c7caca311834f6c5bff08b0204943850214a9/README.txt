commit 351c7caca311834f6c5bff08b0204943850214a9
Author: Stefania Alborghetti <stefania.alborghetti@datastax.com>
Date:   Thu Aug 27 14:09:45 2015 +0800

    Handle non-atomic directory streams safely (CASSANDRA-10109)

    This patch refactors the lifecycle transaction log and updates
    the logic to be robust to non-atomic listings of directories

    patch by stefania; reviewed by benedict for CASSANDRA-10109