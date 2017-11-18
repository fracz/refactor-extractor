commit 9210bc85545f31973c957b5179e6a82d05f473c6
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Sep 5 12:31:16 2013 -0700

    Implement #10744011: Serialize running of background services

    Added some code to the activity manager to keep track of
    services that are launching and limit the number that can
    be launched concurrently.  This only comes into play under
    specific circumstances: when the service launch is a background
    request (so timing is not important) and its process is not
    already running at a high priority.

    In this case, we have a list of services that are currently
    launching and when that gets too big we start delaying the
    launch of future services until currently launching ones are
    finished.

    There are some important tuning parameters for this: how many
    background services we allow to launch concurrently (currently
    1 on low-ram devices, 3 on other devices), and how long we
    wait for a background service to run before consider it to be
    a more long-running service and go on to the next pending
    launch (currently set to 15 seconds).

    Also while in here, did some cleanup of the service code:

    - A little refactoring to make per-user data cleaner.
    - Switch to ArrayMap.

    Change-Id: I09f372eb5e0f81a8de7c64f8320af41e84b90aa3