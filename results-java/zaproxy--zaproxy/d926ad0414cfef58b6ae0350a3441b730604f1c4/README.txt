commit d926ad0414cfef58b6ae0350a3441b730604f1c4
Author: thc202 <thc202@gmail.com>
Date:   Mon Sep 7 09:17:15 2015 +0100

    Start GUI on the EventQueue (and refactor ZAP's bootstrap process)

    Swing GUI should be started on the EDT (Swing requirement), otherwise it
    might lead to unexpected issues. For example, with ZAP, during start it
    could lead to class cast exceptions, ArrayIndexOutOfBoundsException and
    other exceptions types, some of them could even crash ZAP (because of
    unhandled exceptions).
    The change starts ZAP's GUI on the EDT to prevent the problems mentioned
    previously also the bootstrap process was refactored to make easier to
    differentiate headless and GUI behaviour, it was used polymorphism to
    start different bootstrap processes (GUI, daemon and command line). ZAP
    class now just instantiates and starts the appropriate boostrap process.
    Change class ExtensionLoader to execute view related code in the EDT.
    Change class LicenseFrame to execute a task once it's closed instead of
    requiring to use a "waiting thread" (to continue with bootstrap process)
    by client code.
    Change class Splashscreen to not be an undecorated window as it might
    cause rendering problems on some OS's (Fedora 21), change it to take
    into account that's run in the EDT and make sure that external calls to
    update the progress and append messages is done in the EDT.