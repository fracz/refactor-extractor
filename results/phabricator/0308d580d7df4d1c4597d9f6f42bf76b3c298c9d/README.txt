commit 0308d580d7df4d1c4597d9f6f42bf76b3c298c9d
Author: epriestley <git@epriestley.com>
Date:   Wed May 18 09:32:50 2016 -0700

    Deactivate SSH keys instead of destroying them completely

    Summary:
    Ref T10917. Currently, when you delete an SSH key, we really truly delete it forever.

    This isn't very consistent with other applications, but we built this stuff a long time ago before we were as rigorous about retaining data and making it auditable.

    In partiular, destroying data isn't good for auditing after security issues, since it means we can't show you logs of any changes an attacker might have made to your keys.

    To prepare to improve this, stop destoying data. This will allow later changes to become transaction-oriented and show normal transaction logs.

    The tricky part here is that we have a `UNIQUE KEY` on the public key part of the key.

    Instead, I changed this to `UNIQUE (key, isActive)`, where `isActive` is a nullable boolean column. This works because MySQL does not enforce "unique" if part of the key is `NULL`.

    So you can't have two rows with `("A", 1)`, but you can have as many rows as you want with `("A", null)`. This lets us keep the "each key may only be active for one user/object" rule without requiring us to delete any data.

    Test Plan:
    - Ran schema changes.
    - Viewed public keys.
    - Tried to add a duplicate key, got rejected (already associated with another object).
    - Deleted SSH key.
    - Verified that the key was no longer actually deleted from the database, just marked inactive (in future changes, I'll update the UI to be more clear about this).
    - Uploaded a new copy of the same public key, worked fine (no duplicate key rejection).
    - Tried to upload yet another copy, got rejected.
    - Generated a new keypair.
    - Tried to upload a duplicate to an Almanac device, got rejected.
    - Generated a new pair for a device.
    - Trusted a device key.
    - Untrusted a device key.
    - "Deleted" a device key.
    - Tried to trust a deleted device key, got "inactive" message.
    - Ran `bin/ssh-auth`, got good output with unique keys.
    - Ran `cat ~/.ssh/id_rsa.pub | ./bin/ssh-auth-key`, got good output with one key.
    - Used `auth.querypublickeys` Conduit method to query keys, got good active keys.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10917

    Differential Revision: https://secure.phabricator.com/D15943