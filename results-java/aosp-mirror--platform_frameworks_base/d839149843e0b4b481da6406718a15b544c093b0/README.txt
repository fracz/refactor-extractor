commit d839149843e0b4b481da6406718a15b544c093b0
Author: Ben Kwa <kenobi@google.com>
Date:   Thu Dec 17 10:37:00 2015 -0800

    Refactor DocumentHolder.

    Primary goals of this refactor are to reduce DirectoryFragment bloat,
    and to simplify the code (especially the binding code) for the different
    layouts.

    - Decouple DocumentHolder from DirectoryFragment.
    - Move it into its own file.
    - Move binding code from DirectoryFragment into DocumentHolder.
    - Split DocumentHolder implementation into three separate subclasses,
      for grid items, list items, and dividers.

    BUG=24326989

    Change-Id: I217bf4e5b8e1b33173b8b0275591a8c5d8e9161c