commit 16e1aff41b29ff935cf469d06168b552ffabeb46
Author: Carsten Brandt <mail@cebe.cc>
Date:   Mon Feb 3 14:01:29 2014 +0100

    renamed ActiveRecord::create() to populateRecord()

    - refactored elasticsearch AR to make type and index available in
      instantiate(), issue #1313

    fixes #2281