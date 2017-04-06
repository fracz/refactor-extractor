commit 06184ee006ac9fa3733eb221e74739d4f3081ec9
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu Mar 31 10:19:44 2016 +0200

    Remove RescoreParseElement

    The refactoring of RescoreBuilder and QueryRescoreBuilder moved parsing
    previously in RescoreParseElement into the builders. This removes the
    left over parse element.