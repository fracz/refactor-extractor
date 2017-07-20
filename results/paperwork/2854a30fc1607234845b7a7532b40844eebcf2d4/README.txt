commit 2854a30fc1607234845b7a7532b40844eebcf2d4
Author: Chris Lahaye <mail@chrislahaye.com>
Date:   Fri Feb 27 23:45:59 2015 +0100

    Filtering by date improved and fixed

    Fixes #260

    - Add ApiCalendarController
    - Add route for api/v1/calendar to ApiCalendarController
    - Add getCalendar method to notebooks.service.js that executes the API
    request
    - Change filter for disabled calendar days in notebooks.controller.js
    - Remove garbage assignment to $rootScope.tags