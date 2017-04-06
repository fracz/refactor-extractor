commit 3f6ed7e1def9b0a3e5d4119f7e11457cb1c8c8f2
Author: kimchy <kimchy@gmail.com>
Date:   Fri Dec 10 00:17:10 2010 +0200

    improve update mapping on master, if we end up with the same mappings as the one the cluster state has, no need for a new cluster state...