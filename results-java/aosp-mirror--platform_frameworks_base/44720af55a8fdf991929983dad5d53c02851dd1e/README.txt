commit 44720af55a8fdf991929983dad5d53c02851dd1e
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Tue Aug 20 16:32:53 2013 -0700

    Print UI bug fixing and printer discovery refactoring.

    1. Added support for selecting a printer from the all printers activity
       that is not in the initial printer selection drop down. The user
       initially sees a sub set of the printers in the drop down and the
       last option is to see all printers in a separate activity. Some
       of the printers in the all printers activity are not shown in the
       initial drop down.

    2. Refactored printer discovery by adding (private for now) printer
       discovery app facing APIs. These APIs are needed to support multiple
       printer selection activities (print dialog and all printers activities)
       and also the settings for showing all printers for a service.

       Now multiple apps can request observing for printers and there is
       a centralized mediator that ensures the same printer discovery
       session is used. The mediator dispatches printer discovery specific
       requests to print services. It also aggregates discovered printers
       and delivers them to the interested apps. The mediator minimizes
       printer discovery session creation and starting and stopping discovery
       by sharing the same discovery session and discovery window with
       multiple apps. Lastly, the mediator takes care of print services
       enabled during discovery by bringing them up to the current
       discovery state (create discovery session and start discovery if
       needed). The mediator also reports disappearing of the printers
       of a service removed during discovery and notifies a newly
       registered observers for the currnet printers if the observers are
       added during an active printer discovery session.

    3. Fixed bugs in the print UI and implemented some UX tweaks.

    Change-Id: I4d0b0c5a6c6f1809b2ba5dbc8e9d63ab3d48f1ef