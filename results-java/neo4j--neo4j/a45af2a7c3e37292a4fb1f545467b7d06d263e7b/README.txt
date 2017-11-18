commit a45af2a7c3e37292a4fb1f545467b7d06d263e7b
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Sat Feb 22 22:13:14 2014 +0100

    Introdude new Locks API, and the Forseti lock manager

     o The classic LockManager remains, but as an implementation of
       the new Locks API. It has been slightly improved to not contend
       on global data structures when taking locks it already holds.

       The classc lock manager is now referred to as the "Community
       Lock Manager".

     o Forseti is a new lock manager, available in enterprise only, it
       is faster than the community manager, and scales linearly as more
       execution threads are added. It uses a new deadlock detection
       scheme as well, which means deadlock detection is significantly
       faster and requires no pessimistic synchronization.