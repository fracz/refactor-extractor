commit a00271533f639c8ed36429c663889ac9f654bc72
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Tue Jun 25 14:59:53 2013 -0700

    Refactoring of the print sub-system and API clean up.

    1. Now a user state has ins own spooler since the spooler app is
       running per user. The user state registers an observer for the state
       of the spooler to get information needed to orchestrate unbinding
       from print serivces that have no work and eventually unbinding from
       the spooler when all no service has any work.

    2. Abstracted a remote print service from the perspective of the system
       in a class that is transparently managing binding and unbinding to
       the remote instance.

    3. Abstracted the remote print spooler to transparently manage binding
       and unbinding to the remote instance when there is work and when
       there is no work, respectively.

    4. Cleaned up the print document adapter (ex-PrintAdapter) APIs to
       enable implementing the all callbacks on a thread of choice. If
       the document is really small, using the main thread makes sense.

       Now if an app that does not need the UI state to layout the printed
       content, it can schedule all the work for allocating resources, laying
       out, writing, and releasing resources on a dedicated thread.

    5. Added info class for the printed document that is now propagated
       the the print services. A print service gets an instance of a
       new document class that encapsulates the document info and a method
       to access the document's data.

    6. Added APIs for describing the type of a document to the new document
       info class. This allows a print service to do smarts based on the
       doc type. For now we have only photo and document types.

    7. Renamed the systemReady method for system services that implement
       it with different semantics to systemRunning. Such methods assume
       the the service can run third-party code which is not the same as
       systemReady.

    8. Cleaned up the print job configuration activity.

    9. Sigh... code clean up here and there. Factoring out classes to
       improve readability.

    Change-Id: I637ba28412793166cbf519273fdf022241159a92