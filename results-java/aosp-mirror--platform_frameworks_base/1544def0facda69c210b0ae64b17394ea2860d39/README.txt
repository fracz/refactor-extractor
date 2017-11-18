commit 1544def0facda69c210b0ae64b17394ea2860d39
Author: Yohei Yukawa <yukawa@google.com>
Date:   Tue Apr 26 17:13:38 2016 -0700

    Fix stale InputMethodManager#mFullscreenMode.

    The current mechanism to sync InputMethodService#mIsFullscreen to
    InputMethodManager#mFullscreenMode is really fragile because
      1. Currently the state change is notified via
         InputConnection#reportFullscreenMode(), where InputConnection is
         designed to be valid only while the IME has input focus to the
         target widget.
      2. In favor of performance InputMethodService (IMS) calls
         InputConnection#reportFullscreenMode() only when #mIsFullscreen
         changed.  If InputConnection#reportFullscreenMode() failed, there
         is no recovery mechanism.
      3. Screen oriantation change is likely to cause Window/View focus
         state change in the target application, which is likely to
         invalidate the current InputConnection.

    What our previous workaround [1] did for Bug 21455064 was actually
    relaxing the rule 1 only for InputConnection#reportFullscreenMode().
    However, my another CL [2] made the lifetime check of InputConnection a
    bit more strict again, which revived the issue as Bug 28157836.

    Probably a long-term fix would be to stop using InputConnection to sync
    that boolean state between IMS and the application.  However, it's too
    late to do such a refactoring in N, hence this CL relaxes the rule 1
    again keeping it as secure as possible.

    The idea is that we allow InputConnection#reportFullscreenMode() to
    update InputMethodManager#mFullscreenMode regardless of whether
    InputConnection is active or not, as long as the InputConnection is
    bound to the curent IME.  Doing this as a short-term solution is
    supporsed to not introduce any new risk because the active IME is
    already able to mess up the InputMethodManager#mFullscreenMode by
    calling InputConnection#reportFullscreenMode() on any other active
    InputConnection.  Bug 28406127 will track the long-term solution.

     [1]: Id10315efc41d86407ccfb0a2d3956bcd7c0909b8
          da589dffddaf4046d3b4fd8d14d5f984a1c4324a
     [2]: If2a03bc84d318775fd4a197fa43acde086eda442
          aaa38c9f1ae019f0fe8c3ba80630f26e582cc89c

    Bug: 28157836
    Change-Id: Iba184245a01a3b340f006bc4e415d304de3c2696