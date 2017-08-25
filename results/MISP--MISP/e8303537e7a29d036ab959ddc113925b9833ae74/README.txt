commit e8303537e7a29d036ab959ddc113925b9833ae74
Author: Iglocska <andras.iklody@gmail.com>
Date:   Fri Jan 20 10:33:38 2017 +0100

    fix: Organisation UI and API improvements

    - opened up the organisations controller to API actions
      - this includes index/add/edit/delete
      - uses the still new-ish standardised REST library
      - send GET requests to add/edit to view the parameters

    - reworked the org index to paginate 60 items instead of 20 and to have a view all button