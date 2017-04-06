commit 5d68c763e241e6e5caae07f9bc9d06cab325c08c
Author: Tero Parviainen <tero@teropa.info>
Date:   Sat May 30 06:15:40 2015 +0100

    refactor($compile): remove unused elementTransclusion argument

    Remove the unused elementTransclusion argument from createBoundTranscludeFn.
    Also remove the nodeLinkFn.elementTranscludeOnThisElement attribute, which
    becomes unnecessary.

    Closes #9962
    Closes #11985