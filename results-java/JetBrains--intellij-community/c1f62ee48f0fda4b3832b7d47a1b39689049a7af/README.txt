commit c1f62ee48f0fda4b3832b7d47a1b39689049a7af
Author: Chris Thunes <cthunes@brewtab.com>
Date:   Sat Mar 25 12:23:19 2017 -0400

    JavaDoc formatting refactor.

    Fixes a couple issues along the way,

    - Disabling "Preserve Line Feeds" has no affect on the JavaDoc
      description unless a line already exceeds the right margin. This is
      inconsistent with how tag blocks are handled (they are generally
      re-flowed regardless of if the right margin has been exceeded).
    - A couple class-level tags (@author and @version) used inconsistent
      continuation indentation from other tags.