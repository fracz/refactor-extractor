commit 9603a65b8c5f6f2e6091d31dccb654a3b57200a5
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Wed Aug 24 16:16:45 2016 +0200

    Allow read queries to run on Pending machines

    Before this change in HA when switching to pending the Locks delegate
    would have set to null, which prevents to create any transactions.
    Indeed, at transaction creation time we create a lock client through
    the Locks for such transaction.  Basically failing all transaction
    creation.

    This commit changes the Locks to be a ReadOnlyLocks when switching to
    pending. Such Locks allows for creation of ReadOnlyClient which fails
    all possible lock acquisition, i.e., write transaction.  In such a way
    read transaction can execute on machine in pending state.

    It also improves the AbstractComponentSwitcher to be less racy by
    switching directly from old delegate to new delegate, without setting
    the delegate to null in between those 2 steps.