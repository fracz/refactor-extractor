commit a70f76f76305e5bdc8161f9836d6850f75442aec
Author: Jason Tedor <jason@tedor.me>
Date:   Sun Jan 3 18:18:07 2016 -0500

    Make cluster state external to o.e.c.a.s.ShardStateAction

    This commit modifies the handling of cluster states in
    o.e.c.a.s.ShardStateAction so that all necessary state is obtained
    externally to the ShardStateAction#shardFailed and
    ShardStateAction#shardStarted methods. This refactoring permits the
    removal of the ClusterService field from ShardStateAction.