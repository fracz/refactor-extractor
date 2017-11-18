commit d43df99d2e69f9519569fb6cdce7b6e1988649c6
Author: Iiro Krankka <iiro.krankka@gmail.com>
Date:   Sat Jul 23 15:23:11 2016 +0300

    Made whole lot of changes:
    - Now the tabs can't be inflated other than using an XML resource.
    - Ditched the PopupMenu shit and menu a custom parser for the tabs.
    - Wrote some actual unit tests for the custom parser!
    - Aiming for a complete refactor with proper testing, so no more regression bugs.