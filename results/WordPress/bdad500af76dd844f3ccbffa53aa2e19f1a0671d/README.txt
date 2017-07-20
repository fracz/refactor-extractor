commit bdad500af76dd844f3ccbffa53aa2e19f1a0671d
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Mon May 22 20:24:41 2017 +0000

    Widgets: Remove the title attributes used in the Tag cloud widget.

    - improves accessibility using an aria-label attribute to match the information conveyed visually with the one exposed to assistive technologies
    - adds an option in the widget to display the item counts, consistently with what other widgets already do (Archives, Categories)

    Props adamsoucie, emirpprime, Samantha Miller., MikeLittle, rianrietveld, sami.keijonen, adamsilverstein, westonruter, afercia.
    See #24766.
    Fixes #35566.

    Built from https://develop.svn.wordpress.org/trunk@40816


    git-svn-id: http://core.svn.wordpress.org/trunk@40674 1a063a9b-81f0-0310-95a4-ce76da25c4cd