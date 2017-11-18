commit 94a87fd71aec26a1501afe89ea4593d2ea0e3c2d
Author: Rickard Öberg <rickardoberg@gmail.com>
Date:   Wed Apr 9 18:34:18 2014 +0800

    Fixed Cypher tests that depended on list order, when set is enough
    Renamed and refactored DenseNodeTransactionTranslator
    Created Lucene translator that just upgrades the version number
    Wired in the translators