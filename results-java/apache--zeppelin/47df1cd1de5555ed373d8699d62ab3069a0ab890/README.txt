commit 47df1cd1de5555ed373d8699d62ab3069a0ab890
Author: eranwitkon <goi.cto@gmail.com>
Date:   Sun Oct 4 01:51:51 2015 +0300

    zeppelin-333:Notebook create, delete & clone REST API

    Initial implementation of createNote REST API.
    implementation overlap socket implementation, to minimal the effect for socket behavior, later PR can refactor the creation "logic" (create note, add paragraph, set name) to the zengine class and avoid duplication or even better migrate web client to use REST API as well.
    Still need to find a way to broadcast note list to all client same as socket does in
    ```broadcastNote(note);``` and ```broadcastNoteList();```

    Author: eranwitkon <goi.cto@gmail.com>

    Closes #334 from eranwitkon/333 and squashes the following commits:

    7eb28a0 [eranwitkon] cleanup TODO
    aa23d31 [eranwitkon] Add cleanup for test of clone REST API
    18f7001 [eranwitkon] Add support for clone Notebook Rest API
    defef05 [eranwitkon] refactor clone note to use zengine.notebook.cloneNote and expose brodcast method as public
    80b885a [eranwitkon] Add Clone existing note to zengine server
    6a8b569 [eranwitkon] initial implementation of Delete notebook REST API
    f61bb8c [eranwitkon] persist note after create to save new paragraph and new name
    310b449 [eranwitkon] Add access to Notebook socket to the NotebookRest API to allow broadcast messages on notebooks changes.
    d1efbab [eranwitkon] fix typo
    d957fa2 [eranwitkon] add documentation for notebook create, clone & delete REST API
    69b3882 [eranwitkon] initial implementation if createNote REST API