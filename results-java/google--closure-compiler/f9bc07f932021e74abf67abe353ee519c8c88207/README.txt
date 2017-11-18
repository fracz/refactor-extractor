commit f9bc07f932021e74abf67abe353ee519c8c88207
Author: tomnguyen <tomnguyen@google.com>
Date:   Sun Aug 14 08:30:18 2016 -0700

    [JS Compiler] Stop the ES6 rewrite arrow function from renaming "this" and "arguments" to their obfuscated version by setting the original name on the node.

    There is no outward facing change for most customers. This is mostly a change to support refactoring tools.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130226178