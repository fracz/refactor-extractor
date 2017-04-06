commit c3070062db7ce1fc5e7278ead94288f1498217bd
Author: javanna <cavannaluca@gmail.com>
Date:   Mon Aug 31 10:05:47 2015 +0200

    Internal: small QueryShardContext cleanup

    Added some comments about changes that we might be able to make later on in the refactoring. Also exposed handleTermsLookup in the context directly, removed unused similarityService getter and corrected simpleMatchToIndexNames call according to changes happened on master (the method variant that accept types will go away, types are ignored anyway).