commit 85cc390189f45be54dec9b146f66eeb7737fb0eb
Author: Alex Petrov <oleksandr.petrov@gmail.com>
Date:   Tue May 24 09:46:43 2016 +0200

    Prevent OOM failures on SSTable corruption, improve tests for corruption detection

    Patch by Alex Petrov; reviewed by Stefania Alborghetti for CASSANDRA-9530