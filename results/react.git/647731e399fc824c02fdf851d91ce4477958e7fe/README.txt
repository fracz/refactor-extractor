commit 647731e399fc824c02fdf851d91ce4477958e7fe
Author: Pete Hunt <pete@instagram.com>
Date:   Mon Sep 9 14:55:42 2013 -0700

    Supporting mounting into iframes

    Sencha says that separating big components into their own iframes was important for performance:
    http://www.sencha.com/blog/the-making-of-fastbook-an-html5-love-story.

    Today the only thing stopping us is that events don't bubble to our events system from an iframe. This diff
    looks at the owning document of the container and adds top-level listeners to it. It should not change
    existing behavior and should improve our support for this.