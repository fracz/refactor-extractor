commit fbaa1968b7c596ccb63ea8b4be1d3bd92eda50d8
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Apr 9 14:27:14 2012 -0700

    chore($browser): remove the addJs method

    this was never meant to be a public api used by apps. I refactored
    the code to hide the functionality.

    BREAKING CHANGE: $browser.addJs method was removed

    apps that depended on this functionality should either use many of the
    existing script loaders or create a simple helper method specific to the
    app.