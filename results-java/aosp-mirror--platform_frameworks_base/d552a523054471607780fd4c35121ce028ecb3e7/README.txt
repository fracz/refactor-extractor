commit d552a523054471607780fd4c35121ce028ecb3e7
Author: Yohei Yukawa <yukawa@google.com>
Date:   Sun Mar 20 15:08:31 2016 -0700

    Add a comment about ResultReceiver lifetime.

    The root cause of memory leak crbug.com/595613 was that Chromium and
    WebView have called InputMethodManager#showSoftInput() with
    ResultReceiver thta has a strong reference to application logic class
    named ContentViewCore.  In this particular case, ResultReceiver in
    question will be copied to InputMethodManagerService and the current
    InputMethodService, which means the original receiver object will not be
    collected until the copied objects in other processes are GCed.

    Probably there might be several ways to oprimize the performance in
    Framework side because the corresponding ResultReceiver objects will be
    always handled within the Android Framework unless IME developers
    explicitly override the following method.
      InputMethodService#onCreateInputMethodInterface()

    Anyway, it is probably a bit too late to do such an optimization in N,
    and application developers need to support existing versions of Android
    anyway.  For N, probably making it clear in JavaDoc would be the only
    realistic improvement we can do.

    Bug: 27658034
    Bug: 27727645
    Change-Id: I6fc6b88c91a4b1e0a29e94b99a9f0e35605c01b2