commit bb564f63f7f490278d51241057b2a21dc3f8ff1f
Author: Daniil Ovchinnikov <daniil.ovchinnikov@jetbrains.com>
Date:   Tue Nov 15 17:23:13 2016 +0300

    [groovy] refactor 'change to operator' inspection

    - use strings instead of IElementTypes
    - get rid of call transformation
    - simplify weird builder
    - highlight only method reference instead of whole method call
    - @NotNull