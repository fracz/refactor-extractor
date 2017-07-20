commit e8b9088c5fcd01a72c5c08b9db1b9a99b7f9ac5d
Author: Mark Jaquith <mark@wordpress.org>
Date:   Fri Jul 12 14:01:39 2013 +0000

    Revisions: Cleanup, bug fixes, refactoring, polish.

    * Hide the tooltip initially.
    * Fix a bug with routing.
    * Further separate the Slider model and view, refactoring its code.
    * More reliance on events than direct calls between areas.
    * Smarter background diff loading (single mode). Loads the diffs closest to your position first.
    * Removed a bunch of manual templating and `render()` methods. Now relies more on the WP Backbone Views functionality.
    * include the requested `id` in `ensure:load`.
    * new trigger: `ensure`, for `ensure()` attempts, regardless of whether they are already loaded.
    * pass along a promise in both `ensure` and `ensure:load`.
    * in `ensure`, remove requests for diffs we aready have

    See #24425.

    git-svn-id: http://core.svn.wordpress.org/trunk@24671 1a063a9b-81f0-0310-95a4-ce76da25c4cd