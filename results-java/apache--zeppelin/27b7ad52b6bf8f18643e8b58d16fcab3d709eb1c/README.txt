commit 27b7ad52b6bf8f18643e8b58d16fcab3d709eb1c
Author: eranwitkon <goi.cto@gmail.com>
Date:   Wed Nov 4 15:09:06 2015 +0200

    [Zeppelin-354] - List notebooks REST API

    replacing PR #367
    This is the implementation, test and documentation of List notebooks REST API.
    The documentation include sample JSON with different looknfeel option and coron options.
    This is ready for review.

    Author: eranwitkon <goi.cto@gmail.com>

    Closes #369 from eranwitkon/354 and squashes the following commits:

    5472620 [eranwitkon] update getNotebook list documentation
    b2dad81 [eranwitkon] revert to original implementation as getNotebookList does not need the exclusionStrategy anymore
    0eff22e [eranwitkon] update getList test
    c2f1c5f [eranwitkon] update getNotebookList to use NotebookServer.generateNotebookInfo function
    d0b6744 [eranwitkon] refactor broadcastNoteList to use a new public generateNotebookList function which will be used in both socket implementation and REST implementation.
    1e3d98c [eranwitkon] List Notebook REST API implementation, Test & documentation
    ee53446 [eranwitkon] List Notebook REST API implementation, Test & documentation