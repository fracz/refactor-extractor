commit 8b002dbac2200f6fee7d1ffa9bc416b23786150a
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Mon Apr 24 21:56:09 2017 +0200

    json schema refactoring: simplify code around JsonSchemaVariantsTreeBuilder - keep information about whether the node is in excluding group in the node itself;
    check that returned schemas are not duplicated; when merging schemas, there is important to specify it explicitly, where the schema pointer should point (was a bug with oneOf/anyOf merge)