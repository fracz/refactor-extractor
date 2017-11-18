commit 9cea757a542c785607d25f3267dd8fd4a19d2c21
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Tue Aug 6 03:40:16 2013 +0200

    improved VersionMatcher contract

    - chain finds the first matcher that canHandle() a selector, then delegates to that matcher
    - canHandle() returns false -> no other methods called
    - clients must not call accept(String,String) if metadata is needed
    - adapted and improved specs, added missing specs
    - cleaned up VersionRangeMatcher (deleted obsolete method, moved methods around)