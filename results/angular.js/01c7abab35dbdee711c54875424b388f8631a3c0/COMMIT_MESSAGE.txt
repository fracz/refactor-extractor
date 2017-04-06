commit 01c7abab35dbdee711c54875424b388f8631a3c0
Author: Misko Hevery <misko@hevery.com>
Date:   Tue Oct 19 15:34:58 2010 -0700

    Fix browser triggering in scenario to always do native events.
     - Also fixed angular.suffix for scenarios
     - refactored click() to browserTrigger()
     - Fixed Rakefile with CSS and jQuery