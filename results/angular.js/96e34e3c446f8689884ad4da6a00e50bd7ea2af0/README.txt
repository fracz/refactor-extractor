commit 96e34e3c446f8689884ad4da6a00e50bd7ea2af0
Author: Georgios Kalpakas <g.kalpakas@hotmail.com>
Date:   Fri Jun 3 15:22:19 2016 +0300

    refactor($resource): explicitly set `$resourceProvider.defaults.cancellable` to false

    Previously, it was just `undefined` and had the same behavior (since we only check for falsy-ness).
    Just making it explicit, that this property is also available on `defaults` and is "not true" by
    default.

    Closes #14711