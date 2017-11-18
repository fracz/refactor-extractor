commit 893390bd580eca39ecd693cb0d76c7bc9b36a11d
Author: Aga Wronska <agawronska@google.com>
Date:   Wed Feb 17 13:50:42 2016 -0800

    Directory fragment refactoring.
    First attempt to to refactor fragments handling, state and app lifecycle.

    The goal was to simplify code by using android built lifecycle
    mechanism, eliminate bugs caused by multiple creation of the fragment,
    see the performance impact and give some fundament for refactoring of
    fragments and activities in the app.

    Search view manager:
        * Remove curentSearch from state
        * Restore search from saved state (ex. after rotation)
        * Rename file  to give the better overview of its purpose

    Directory fragment:
        * Store selection state in a bundle
        * Remove double creation of fragment
        * Use loaders to reload content when possible
        * Keep info about state inside the object
        * Refactor available types of fragment to be normal and recents
        * Make search type a mode possibly available in all types
        * Remove search being invoked from refresh method
        * Do search by reloading fragments content instead of recreation as
          an example

    Other:
        * Fix window title maybe

    Bug: 26968405, 27101786
    Change-Id: I58f36cd0a3e3a6ec98996cd8aac16e10e425e1fe