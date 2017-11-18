commit ac84d3ba81f08036308b17e1ab919e43987a3df5
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Mon Apr 4 16:09:08 2011 -0700

    Touch exploration feature, event bubling, refactor

    1. Added an Input Filter that interprets the touch screen motion
       events to perfrom accessibility exploration. One finger explores.
       Tapping within a given time and distance slop on the last exlopred
       location does click and long press, respectively. Two fingers close
       and in the same diretion drag. Multiple finglers or two fingers in
       different directions or two fingers too far away are delegated to
       the view hierarchy. Non moving fingers "accidentally grabbed the
       device for the scrren" are ignored.

    2. Added accessibility events for hover enter, hover exit, touch
       exoloration gesture start, and end. Accessibility hover events
       are fired by the hover pipeline. An accessibility event is
       dispatched up the view tree and the topmost view fires it.
       Thus predecessors can augment the fired event. An accessibility
       event has several records and a predecessor can optionally
       modify, delete, and add such to the event.

    3. Added onPopulateAccessibilityEvent and refactored the existing
       accessibility code to use it.

    4. Added API for querying the currently enabled accessibility services
       by feedback type.

    Change-Id: Iec03c6c3fe298de3f14cb6efdbb9b198cd531a0c