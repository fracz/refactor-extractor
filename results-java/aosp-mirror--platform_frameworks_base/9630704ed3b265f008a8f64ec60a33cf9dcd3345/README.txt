commit 9630704ed3b265f008a8f64ec60a33cf9dcd3345
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Jul 27 15:51:34 2012 -0700

    Power manager rewrite.

    The major goal of this rewrite is to make it easier to implement
    power management policies correctly.  According, the new
    implementation primarily uses state-based rather than event-based
    triggers for applying changes to the current power state.

    For example, when an application requests that the proximity
    sensor be used to manage the screen state (by way of a wake lock),
    the power manager makes note of the fact that the set of
    wake locks changed.  Then it executes a common update function
    that recalculates the entire state, first looking at wake locks,
    then considering user activity, and eventually determining whether
    the screen should be turned on or off.  At this point it may
    make a request to a component called the DisplayPowerController
    to asynchronously update the display's powe state.  Likewise,
    DisplayPowerController makes note of the updated power request
    and schedules its own update function to figure out what needs
    to be changed.

    The big benefit of this approach is that it's easy to mutate
    multiple properties of the power state simultaneously then
    apply their joint effects together all at once.  Transitions
    between states are detected and resolved by the update in
    a consistent manner.

    The new power manager service has is implemented as a set of
    loosely coupled components.  For the most part, information
    only flows one way through these components (by issuing a
    request to that component) although some components support
    sending a message back to indicate when the work has been
    completed.  For example, the DisplayPowerController posts
    a callback runnable asynchronously to tell the PowerManagerService
    when the display is ready.  An important feature of this
    approach is that each component neatly encapsulates its
    state and maintains its own invariants.  Moreover, we do
    not need to worry about deadlocks or awkward mutual exclusion
    semantics because most of the requests are asynchronous.

    The benefits of this design are especially apparent in
    the implementation of the screen on / off and brightness
    control animations which are able to take advantage of
    framework features like properties, ObjectAnimator
    and Choreographer.

    The screen on / off animation is now the responsibility
    of the power manager (instead of surface flinger).  This change
    makes it much easier to ensure that the animation is properly
    coordinated with other power state changes and eliminates
    the cause of race conditions in the older implementation.

    The because of the userActivity() function has been changed
    so that it never wakes the device from sleep.  This change
    removes ambiguity around forcing or disabling user activity
    for various purposes.  To wake the device, use wakeUp().
    To put it to sleep, use goToSleep().  Simple.

    The power manager service interface and API has been significantly
    simplified and consolidated.  Also fixed some inconsistencies
    related to how the minimum and maximum screen brightness setting
    was presented in brightness control widgets and enforced behind
    the scenes.

    At present the following features are implemented:

    - Wake locks.
    - User activity.
    - Wake up / go to sleep.
    - Power state broadcasts.
    - Battery stats and event log notifications.
    - Dreams.
    - Proximity screen off.
    - Animated screen on / off transitions.
    - Auto-dimming.
    - Auto-brightness control for the screen backlight with
      different timeouts for ramping up versus ramping down.
    - Auto-on when plugged or unplugged.
    - Stay on when plugged.
    - Device administration maximum user activity timeout.
    - Application controlled brightness via window manager.

    The following features are not yet implemented:

    - Reduced user activity timeout for the key guard.
    - Reduced user activity timeout for the phone application.
    - Coordinating screen on barriers with the window manager.
    - Preventing auto-rotation during power state changes.
    - Auto-brightness adjustment setting (feature was disabled
      in previous version of the power manager service pending
      an improved UI design so leaving it out for now).
    - Interpolated brightness control (a proposed new scheme
      for more compactly specifying auto-brightness levels
      in config.xml).
    - Button / keyboard backlight control.
    - Change window manager to associated WorkSource with
      KEEP_SCREEN_ON_FLAG wake lock instead of talking
      directly to the battery stats service.
    - Optionally support animating screen brightness when
      turning on/off instead of playing electron beam animation
      (config_animateScreenLights).

    Change-Id: I1d7a52e98f0449f76d70bf421f6a7f245957d1d7