commit c4719175f9bce0b235645d6c311219f60ed69ef3
Author: Ilya Chernikov <ilya.chernikov@jetbrains.com>
Date:   Sun Sep 6 13:53:38 2015 +0200

    Refactoring startup and shutdown, refactoring service implementation, implementing error and info reporting to compiler output, idle autoshutdown mechanisms, fixing TargetId serializability, some other refactoring
    Fixing stream to log handler (by removing non-working optimization), fixing idle time calculation, reporting refactorings