commit f535c2702493c7d4415a1fce7bdae13cedad1186
Author: Adrien Grand <jpountz@gmail.com>
Date:   Wed Dec 16 11:28:32 2015 +0100

    Make mapping updates more robust.

    This changes a couple of things:

    Mappings are truly immutable. Before, each field mapper stored a
    MappedFieldTypeReference that was shared across fields that have the same name
    across types. This means that a mapping update could have the side-effect of
    changing the field type in other types when updateAllTypes is true. This works
    differently now: after a mapping update, a new copy of the mappings is created
    in such a way that fields across different types have the same MappedFieldType.
    See the new Mapper.updateFieldType API which replaces MappedFieldTypeReference.

    DocumentMapper is now immutable and MapperService.merge has been refactored in
    such a way that if an exception is thrown while eg. lookup structures are being
    updated, then the whole mapping update will be aborted. As a consequence,
    FieldTypeLookup's checkCompatibility has been folded into copyAndAddAll.

    Synchronization was simplified: given that mappings are truly immutable, we
    don't need the read/write lock so that no documents can be parsed while a
    mapping update is being processed. Document parsing is not performed under a
    lock anymore, and mapping merging uses a simple synchronized block.