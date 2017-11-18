commit fe762344f4475a3a336bb46aef2d59c1fabf32ab
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Thu Oct 13 14:33:27 2016 +0200

    The big keyguard transition refactor (1/n)

    The heart of this change are two things:
    1) Instead of using the force hide mechanism to hide windows behind
    Keyguard, we actually make the activities invisible in activity manager.
    2) When Keyguard is going away, we change the visibilities in activity
    manager and run an app transition.

    At the very core we move the responsibility of hiding activities to
    ActivityStack, which checks whether Keyguard is showing, and then
    hides all non-show-when-locked activities. For that, we need to check
    whether any window of an activity has SHOW_WHEN_LOCKED set. We
    introduce a callback from WM -> AM in case these Keyguard flags have
    changed.

    Furthermore, we decide whether to occlude Keyguard in KeyguardController,
    which just checks whether the top activity has SHOW_WHEN_LOCKED set. When
    this state changes, we prepare an occlude/unocclude app transition, and
    in PWM we just inform the Keyguard about the animation so SysUI can play
    along this animations in a mostly synchronized manner.

    Since we now use an app transition when unlocking the phone, we get
    lockscreen launch animations for free - window manager automatically
    waits until the activity is drawn, or directly executes the transition
    if there is nothing to animate. Thus, we can remove all the infrastructure
    around "waitingForActivityDrawn".

    The logic to show/hide non-app windows is moved to policy, and we add the
    ability to run animations on non-app windows when executing an app
    transition.

    Test:
    1) runtest frameworks-services -c com.android.server.wm.AppTransitionTests
    2) Manually test unlocking Keyguard:
    2a) Without security
    2b) With security
    2c) With security but trusted
    2d) Portrait while activity behind is in landscape
    3) Test launching things from Keyguard
    3a) Without security
    3b) With security
    3c) Launch camera without security
    3d) Launch camera with security
    3e) Launch camera with securtiy and trusted
    3f) Launch voice affordance
    4) Set no notifications on lockscreen, drag down, make sure you get
    the correct animation
    5) Test clicking "emergency" on bouncer
    5b) Test "Emergency info" on emergency dialer
    5c) Test clicking edit button on emergency info, should show pattern on
    Keyguard

    Bug: 32057734
    Change-Id: Icada03cca74d6a612c1f988845f4d4f601087558