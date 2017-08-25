commit f9709bf86b1f552dfd867c5d28ac199e06ac05d2
Author: Borek Bernard <borekb@gmail.com>
Date:   Fri Jun 30 01:08:20 2017 +0200

    'edit' -> 'update' for most actions. Some noteworthy details:

    - Posts had both `edit` and `update` actions but I couldn't find where the `update` one was used. So there's now just a single `update` action, replacing the `edit` one as with other scopes.
    - One exception where both `update` and `edit` are valid are plugins and themes: the `update` action means updating to a new version, `edit` is editing plugin / theme files.
    - `EditActionChangeInfoPreprocessor` class renamed to `UpdateActionChangeInfoPreprocessor` and refactored a bit to fit the new naming.