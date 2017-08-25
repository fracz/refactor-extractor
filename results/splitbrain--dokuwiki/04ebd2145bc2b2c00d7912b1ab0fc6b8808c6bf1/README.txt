commit 04ebd2145bc2b2c00d7912b1ab0fc6b8808c6bf1
Author: chris <chris@jalakai.co.uk>
Date:   Thu Jan 25 15:41:03 2007 +0100

    create an addPluginCall() method for the handler

    refactor plugin() method to use new function

    this provides an interface for plugins to write their own instructions
    directly when returning a single instruction is not sufficient and saves
    plugin authors from hacking the handler's calls stack

    darcs-hash:20070125144103-9b6ab-3df781ec7fd1c4e75ac198139581cd2759c91052.gz