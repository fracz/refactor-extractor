commit 2b8fa64cf74c06733a891de3cf9d0f0904d8996a
Author: Adrien Grand <jpountz@gmail.com>
Date:   Fri Apr 21 10:38:36 2017 +0200

    ESIntegTestCase.indexRandom should not introduce types. (#24202)

    Since we plan on removing types, `indexRandom` should not introduce new types.
    This commit refactors `indexRandom` to reuse existing types.