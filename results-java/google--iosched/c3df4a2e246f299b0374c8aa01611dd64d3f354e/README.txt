commit c3df4a2e246f299b0374c8aa01611dd64d3f354e
Author: Natalie Masse <nmasse@google.com>
Date:   Tue Jan 19 11:48:35 2016 +0000

    Refactored MySchedule for readability

    1. Added a fragment for wide layout
    2. Renamed fragment/views/adapter
    3. Added javadoc to explain how the fragments/views are used
    4. Refactored ViewPagerAdapter out of Activity
    5. Light refactoring in activity (renaming methods etc)
    6. Added javadoc to activity
    7. Moved methods to set/get current time from UIUtils to TimeUtils
    8. Added method to TimeUtils to easily set current time to x ms
       before/after start/end/start of second day of conference
    9. Added UI tests
    10. Fixed bug of tab not changing highlight when swiping horizontally

    Change-Id: I3a5991eec0bc7835254928dd0370855e9e61078d