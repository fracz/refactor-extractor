commit 76b7d585e03b78725f8a276714b233a14cd9c217
Author: Yuri Schimke <yuri@schimke.ee>
Date:   Sun Aug 6 20:19:03 2017 +0100

    Handle h2 vs http/1.1 event ordering differences (#3504)

    * Handle h2 vs http/1.1 event ordering differences

    * remove sout

    * additional test case

    * dont change client, change server protocols

    * refactor test