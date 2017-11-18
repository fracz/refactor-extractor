commit 5d95c6b7a95dca796523a8495e2adaa6621a491d
Author: tbreisacher <tbreisacher@google.com>
Date:   Thu Sep 25 13:48:37 2014 -0700

    refactor: Move newQualifiedNameNode and newName methods back to NodeUtil but rename them and make them take an AbstractCompiler instead of a CodingConvention so that calls are still not too verbose.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=76367877