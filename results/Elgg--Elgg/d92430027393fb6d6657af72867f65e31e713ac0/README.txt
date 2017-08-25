commit d92430027393fb6d6657af72867f65e31e713ac0
Author: Steve Clay <steve@mrclay.org>
Date:   Wed Dec 2 09:13:48 2015 -0500

    perf(files): ElggFile no longer queries metadata for filestore data

    For each persisted `ElggFile` entity loaded, `getFilestore()` used to be called
    on instantiation, which required a separate metadata query.

    This refactors so `getFilestore()` is called only when needed, and typically when
    Elgg's metadata cache has already been populated for the entity.

    Fixes #9138