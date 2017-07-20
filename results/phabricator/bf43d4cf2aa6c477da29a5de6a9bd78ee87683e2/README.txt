commit bf43d4cf2aa6c477da29a5de6a9bd78ee87683e2
Author: epriestley <git@epriestley.com>
Date:   Fri Jan 29 05:21:41 2016 -0800

    Don't mutate DOM on touch-originated cursor events in Differential

    Summary:
    Fixes T10229. Broadly:

      - When the user hovers over a line number or inline comment, we update the yellow reticle to highlight the relevant lines. Specifically, this is in response to a `mouseover` event.
      - On touch devices, touches fire `mouseover` and if you mutate the DOM inside the event, the device aborts the touch.

    To remedy this:

      - Distingiush between mouse-originated and touch-originated cursor events.
        - We do this, roughly, by setting a flag when we see "touchstart", and clearing it when we see the second copy of any unique cursor event.
        - This method is complex, but should be robust to any implementation differences between devices (for example, it will work no matter which order the events are fired in).
        - This method should also produce the correct results on weird devices that have both mouse-devices and touch-devices available for cursor input.
      - When we see a touch-originated `mouseover` or `mouseout`, don't mutate the DOM.
      - Put an extra DOM mutation into the `click` event to improve highlighting behavior on touch devices.

    Test Plan:
      - In iOS Simulator (4s, iOS 9.2), clicked various inline actions ("Reply", "Hide", "Done", "Cancel", line numbers, etc). Got responses after a single touch.
      - Verified hover + click behavior on a desktop.
      - Logged and examined a bunch of events as a general sanity check.

    Reviewers: chad

    Reviewed By: chad

    Subscribers: aljungberg

    Maniphest Tasks: T10229

    Differential Revision: https://secure.phabricator.com/D15136