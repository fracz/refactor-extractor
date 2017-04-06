commit 0c70a9d5bd6c6fe230bdcf1bf33351161504c3b2
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Mar 29 18:36:09 2016 +0200

    fix bug introduced with refactoring of DiscoveryNode constructors

    Transport client was replacing the address of the nodes connecting to with the ones received from the liveness api rather keeping the original listed nodes. Written a test for that.