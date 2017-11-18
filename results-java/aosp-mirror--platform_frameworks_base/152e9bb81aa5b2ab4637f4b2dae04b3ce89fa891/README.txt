commit 152e9bb81aa5b2ab4637f4b2dae04b3ce89fa891
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Fri Oct 12 20:15:29 2012 -0700

    Refactoring of the screen magnification feature.

    1. The screen magnification feature was implemented entirely as a part of the accessibility
       manager. To achieve that the window manager had to implement a bunch of hooks for an
       external client to observe its internal state. This was problematic since it dilutes
       the window manager interface and allows code that is deeply coupled with the window
       manager to reside outside of it. Also the observer callbacks were IPCs which cannot
       be called with the window manager's lock held. To avoid that the window manager had
       to post messages requesting notification of interested parties which makes the code
       consuming the callbacks to run asynchronously of the window manager. This causes timing
       issues and adds unnecessary complexity.

       Now the magnification logic is split in two halves. The first half that is responsible
       to track the magnified portion of the screen and serve as a policy which windows can be
       magnified and it is a part of the window manager. This part exposes higher level APIs
       allowing interested parties with the right permissions to control the magnification
       of a given display. The APIs also allow a client to be registered for callbacks on
       interesting changes such as resize of the magnified region, etc. This part servers
       as a mediator between magnification controllers and the window manager.

       The second half is a controller that is responsible to drive the magnification
       state based on touch interactions. It also presents a highlight when magnified to
       suggest the magnified potion of the screen. The controller is responsible for auto
       zooming out in case the user context changes - rotation, new actitivity. The controller
       also auto pans if a dialog appears and it does not interesect the magnified frame.

    bug:7410464

    2. By design screen magnification and touch exploration work separately and together. If
       magnification is enabled the user sees a larger version of the widgets and a sub section
       of the screen content. Accessibility services use the introspection APIs to "see" what
       is on the screen so they can speak it, navigate to the next item in response to a
       gesture, etc. Hence, the information returned to accessibility services has to reflect
       what a sighted user would see on the screen. Therefore, if the screen is magnified
       we need to adjust the bounds and position of the infos describing views in a magnified
       window such that the info bounds are equivalent to what the user sees.

       To improve performance we keep accessibility node info caches in the client process.
       However, when magnification state changes we have to clear these caches since the
       bounds of the cached infos no longer reflect the screen content which just got smaller
       or larger.

       This patch propagates not only the window scale as before but also the X/Y pan and the
       bounds of the magnified portion of the screen to the introspected app. This information
       is used to adjust the bounds of the node infos coming from this window such that the
       reported bounds are the same as the user sees not as the app thinks they are. Note that
       if magnification is enabled we zoom the content and pan it along the X and Y axis. Also
       recomputed is the isVisibleToUser property of the reported info since in a magnified
       state the user sees a subset of the window content and the views not in the magnified
       viewport should be reported as not visible to the user.

    bug:7344059

    Change-Id: I6f7832c7a6a65c5368b390eb1f1518d0c7afd7d2