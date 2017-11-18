commit ab168996b6b5ec4b9567242664bd9f4d9b23e8f1
Author: Adrien Grand <jpountz@gmail.com>
Date:   Wed Apr 20 09:39:53 2016 +0200

    Fix cross type mapping updates for `boolean` fields. #17882

    Boolean fields were not handled in `DocumentParser.createBuilderFromFieldType`.
    This also improves the logic a bit by falling back to the default mapping of
    the given type insteah of hard-coding every case and throws a better exception
    than a NPE if no dynamic mappings could be created.

    Closes #17879