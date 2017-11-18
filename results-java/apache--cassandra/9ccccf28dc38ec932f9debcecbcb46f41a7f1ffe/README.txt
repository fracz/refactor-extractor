commit 9ccccf28dc38ec932f9debcecbcb46f41a7f1ffe
Author: Stefania Alborghetti <stefania.alborghetti@datastax.com>
Date:   Thu Jul 28 11:12:48 2016 +0800

    avoid deleting non existing sstable files and improve related log messages

    patch by Stefania Alborghetti; reviewed by Benjamin Lerer for CASSANDRA-12261