commit 3ca6cfa471aff43f71abf46672cae43996b8fdbf
Author: daz <daz@bigdaz.com>
Date:   Fri Apr 5 13:26:19 2013 -0600

    Major refactoring of internals involved in UpToDate checking and incremental tasks
    - Changed TaskUpToDateState to fire changes against a specific UpToDateChangeListener, which provides ability to stop accepting further changes.
    - Replaced CompositeUpToDateState with SummaryUpToDateState
      - does not implement TaskUpToDateState
      - Only provides changes from a single underlying source, and limits the number returned
    - Most TaskUpToDateState implementations extend SimpleUpToDateState
       - Pulls all changes into a collection before firing change events
       - Caches all changes for subsequent calls
       - Output files currently use this mechanism: this will be changed soon
    - InputFilesUpToDateState now caches changes from earlier invocations, and resumes parsing from the last change
       - FileCollectionSnapshot.changesSince takes a SnaphostChangeListener, that permits cancellation and resuming again from a particular change.