commit e0a8aabd795736f16cae4aa1d2a68b95e2f5b30f
Author: Daisuke Miyakawa <dmiyakawa@google.com>
Date:   Wed Jun 15 08:37:09 2011 -0700

    Add API for auto-complete ranking improvement.

    - primary account (3510087)
    - per-method ranking (4371572)

    Primary account enables to ContactsProvider to prioritize
    email addresses associated with the account.

    Per-method ranking enables ContactsProvider to get feedback
    from applications about usage per method instead of that per
    person.

    Both should be available for unbundled applications.

    Bug: 3510087
    Bug: 4371572
    Change-Id: Icb01415317a7dbb6538804181b06bd973af6205c