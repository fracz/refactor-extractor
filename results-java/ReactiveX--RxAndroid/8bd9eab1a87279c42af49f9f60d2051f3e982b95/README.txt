commit 8bd9eab1a87279c42af49f9f60d2051f3e982b95
Author: Matthias Kaeppler <m.kaeppler@gmail.com>
Date:   Wed Feb 26 17:41:23 2014 +0100

    A number of improvements to OperatorObserveFromAndroidComponent

    - move the UI thread assert out of the operator and into the helpers; this way, we don't fail the observer anymore with an exception, but the caller.
    - do not loop unsubscribe through the main thread anymore. This unnecessarily defers releasing the references, and might in fact be processed only after Android creates the component after a rotation change. I had to make the references volatile for this to work.
    - immediately unsubscribe in case we detect the componentRef has become invalid. This solves the problem that dangling observers would continue to listen to notifications with no observer alive anymore.

    refs:
    https://github.com/Netflix/RxJava/issues/754
    https://github.com/Netflix/RxJava/issues/899