commit b26adbef4c05a58efd40117cc46a3e818c1bee60
Author: tjhunt <tjhunt>
Date:   Thu Jan 10 10:58:16 2008 +0000

    MDL-12373 - More instances of links to the participants list being shown in the navigation bar to people without the necessary capability.

    I have not copied and pasted the same code into lots of different places. That sucks. We really need to refactor this into a function that builds the navigation up to, and including the user's name. However, I don't have time now. A list of the places touched by this bug (MDL-12373) will at least give a complete list of places that such a refactoring would have to touch.