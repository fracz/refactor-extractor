commit 79311c4af8b54d3cd47ab37a120c648bfc990511
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Tue Jan 17 20:24:26 2012 -0800

    Speedup the accessibility window querying APIs and clean up.

    1. Now when an interrogating client requires an AccessibilibtyNodeInfo
       we aggressively prefetch all the predecessors of that node and its
       descendants. The number of fetched nodes in one call is limited to
       keep the APIs responsive. The prefetched nodes infos are cached in
       the client process. The node info cache is invalidated partially or
       completely based on the fired accessibility events. For example,
       TYPE_WINDOW_STATE_CHANGED event clears the cache while
       TYPE_VIEW_FOCUSED removed the focused node from the cache, etc.
       Note that the cache is only for the currently active window.
       The ViewRootImple also keeps track of only the ids of the node
       infos it has sent to each querying process to avoid duplicating
       work. Usually only one process will query the screen content
       but we support the general case. Also all the caches are
       automatically invalidated so not additional bookkeeping is
       required. This simple strategy leads to 10X improving the
       speed of the querying APIs.

    2. The Monkey and UI test automation framework  were registering a
       raw event listener for accessibility events and hence perform
       connection and cache management in similar way to an AccessibilityService.
       This is fragile and requires the implementer to know internal framework
       stuff. Now the functionality required by the Monkey and the UI automation
       is encapsulated in a new UiTestAutomationBridge class. To enable this
       was requited some refactoring of AccessibilityService.

    3. Removed the *doSomethiong*InActiveWindow methods from the
       AccessibilityInteractionClient and the AccessibilityInteractionConnection.
       The function of these methods is implemented by the not *InActiveWindow
       version while passing appropriate constants.

    4. Updated the internal window Querying tests to use the new
       UiTestAutomationBridge.

    5. If the ViewRootImple was not initialized the querying APIs of
       the IAccessibilityInteractionConnection implementation were
       returning immediately without calling the callback with null.
       This was causing the client side to wait until it times out. Now
       the client is notified as soon as the call fails.

    6. Added a check to guarantee that Views with AccessibilityNodeProvider
       do not have children.

    bug:5879530

    Change-Id: I3ee43718748fec6e570992c7073c8f6f1fc269b3