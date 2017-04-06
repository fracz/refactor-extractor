commit cebd3ca9c3cffb1e5d999a3a51674e13aec3cd30
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Sep 5 17:59:37 2013 +0200

    improve setting response/failure on nodes actions
    we can use the index in the node ids list as the index for the array when we set the response or the exception, removing the need for an index AtomicInteger