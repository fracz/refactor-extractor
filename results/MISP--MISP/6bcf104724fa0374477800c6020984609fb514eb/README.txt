commit 6bcf104724fa0374477800c6020984609fb514eb
Author: Iglocska <andras.iklody@gmail.com>
Date:   Tue Sep 29 02:54:25 2015 +0200

    Progress on several features

    - implemented a custom pagination tool for data sets that are not directly taken from teh db
      - currently creates a pagination object that mocks CakePHP pagination
      - supports the CakePHP pagination view helper
      - supports: pagination, sorting, custom filters

    - implemented first step of the remote instance browser for admins
      - view an index of events on another instance
      - filter the events
      - uses the new pagination

    - still missing:
      - remote event view
      - fetch event from remote instance

    - reworked the event view
      - separated API and UI code path
        - major speedup for the API!
        - cleaner code as there was almost 0 overlap
      - discussions and attributes are now loaded separately from the event view
        - added after the event view loads via ajax
        - cleaner pagination
      - attribute pagination now finally allows for sorting
        - future improvement (coming soon): Show proposals only filter
        - filtering on the attributes in general