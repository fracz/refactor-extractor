commit bb6526966b959194fa56418b06c58312a7b4a600
Author: diosmosis <benakamoorthi@fastmail.fm>
Date:   Mon Jul 21 01:18:57 2014 -0700

    Fixes #5807, refactor CronArchive class so it does not configure itself based on command line arguments. It now has properties that must be set. The core:archive command has been refactored to set these properties based on command line args.

    Also includes:

      - bug fix to CronArchive::initLog method (config was modified after log instance created which negated it's effect)
      - misc/archive.php no longer uses Archive.php but executes the core:archive console command using Piwik\Console
      - remove forcelogtoscreen parameter (no longer necessary due to initLog changes)