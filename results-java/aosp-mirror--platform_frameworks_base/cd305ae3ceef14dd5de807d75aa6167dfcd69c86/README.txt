commit cd305ae3ceef14dd5de807d75aa6167dfcd69c86
Author: Alan Viverette <alanv@google.com>
Date:   Fri Dec 12 14:13:24 2014 -0800

    Give accessibility delegate the first pass at handling ACTION_CLICK

    Delegation is broken for widgets, but this fixes the most egregious issue
    where TextViews that are top-level list items weren't handling CLICK
    actions correctly. This will still need work, since now the focus action
    won't run, but it's an improvement.

    BUG: 18736135
    Change-Id: I808ef628198946cc87f13c53d6245cd162a1e517