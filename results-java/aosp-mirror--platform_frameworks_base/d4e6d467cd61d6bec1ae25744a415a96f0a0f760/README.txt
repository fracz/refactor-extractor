commit d4e6d467cd61d6bec1ae25744a415a96f0a0f760
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri May 16 16:32:37 2014 -0700

    Delay the dispatching of non-wakeup alarms.

    When the screen is off, there are no guarantees about when
    non-wakeup alarms will be dispatched.  Historically they are
    dispatched any time the device wakes up.  With this change,
    we will delay the dispatch until sometime later.

    The amount of delay is determined by how long the screen has
    been off.  Currently there are three possible delays: up to
    2 minutes if the screen has been off for less than 5 minutes;
    up to 15 minutes if it has been off for less than 30 minutes;
    and otherwise up to an hour.

    When the screen is turned on or a wakeup alarm is dispatched,
    all delayed alarms will also be dispatched.

    Note that one of the things this delays is TIME_TICK, which
    means the in many cases we won't deliver TIME_TICK until the
    screen is in the process of waking up.  The current
    implementation causes this to be delayed until the SCREEN_ON
    broadcast is sent; we probably want to improve this to have
    the power manager tell the alarm manager about the screen
    turning on before it sends that broadcast, to help make sure
    things like the lock screen can update their current time
    before the screen is actually turned on.

    In addition, switch all of the alarm stats to use the new
    PendingIntent "tag" identifier for its operations, instead
    of the old code to try to construct a pseudo-identifier
    by retrieving the raw Intent.

    Also add a new package manager command to immediately write
    packages.xml.

    Change-Id: Id4b14757cccff9cb2c6b36de994de38163abf615