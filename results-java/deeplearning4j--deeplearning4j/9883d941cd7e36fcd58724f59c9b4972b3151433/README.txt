commit 9883d941cd7e36fcd58724f59c9b4972b3151433
Author: Darren Foong <darrenfoong@gmail.com>
Date:   Fri Mar 11 10:33:35 2016 +0000

    UNK refactoring

    Previously static UNK is renamed to DEFAULT_UNK, with external
    static references modified accordingly.

    New UNK is private String; WordVectors and WordVectorsImpl modified
    accordingly.

    getWordVector() and getWordVectorMatrixNormalized() return null for
    unknown words.


    Former-commit-id: 8d7d402411526e0827b7fbd6ae0db2da00cc9bbd