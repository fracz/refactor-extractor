commit 4693b251718442ed0903f13326652a4aa529dfcd
Author: Dominik Schilling <dominikschilling+git@gmail.com>
Date:   Wed Feb 17 15:22:26 2016 +0000

    Update Backbone and Underscore to the latest versions.

    Backbone, from 1.1.2 to 1.2.3. Underscore, from 1.6.0 to 1.8.3.

    The new versions of Backbone and Underscore offer numerous small bug fixes and some optimizations and other improvements. Check the [http://backbonejs.org/#changelog Backbone changelog] and [http://underscorejs.org/#changelog Underscore changelog] for the full details.

    The new versions include some significant changes that may break existing code. Plugins or themes that rely on the bundled Backbone and/or Underscore libraries should carefully check functionality with the latest versions and run any available unit tests to ensure compatibility.

    Some changes of note that were addressed in core as part of this upgrade:

    * `_.flatten` no longer works with objects since Underscore.js 1.7. `_.flatten()` working with objects was an unintended side-affect of the implementation, see [https://github.com/jashkenas/underscore/issues/1904#issuecomment-60241576 underscore#1904]. Check any `_flatten` usage and only flatten arrays.
    * As of Backbone 1.2.0, you can no longer modify the `events` hash or your view's `el` property in `initialize`, so don't try to modify them there.
    * Since Underscore 1.7, Underscore templates no longer accept an initial data object. `_.template` always returns a function now so make sure you use it that way.

    Props adamsilverstein.
    Fixes #34350.
    Built from https://develop.svn.wordpress.org/trunk@36546


    git-svn-id: http://core.svn.wordpress.org/trunk@36513 1a063a9b-81f0-0310-95a4-ce76da25c4cd