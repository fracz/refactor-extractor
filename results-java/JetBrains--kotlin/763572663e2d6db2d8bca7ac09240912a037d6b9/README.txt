commit 763572663e2d6db2d8bca7ac09240912a037d6b9
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jul 24 15:28:18 2013 +0400

    Delete almost all code related to signature writing

    Delete org.jetbrains.jet.rt.signature and jet.typeinfo packages from runtime,
    refactor BothSignatureWriter so that it now writes only java generic signature
    and saves parameter types and kinds