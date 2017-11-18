commit e5cdeec2b7706aefd1bb88cd62409a0591ca2f0f
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Mon Nov 18 18:21:47 2013 +0100

    Initialize transactions on master as late as possible.

    o Remote transaction is no longer created on enlist, because read transactions are now enlisted as well, plummeting performance.
      Instead, the slave tracks a flag for whether the remote transaction is initialized or not, and then only initializes on two points,
      either when the first lock is grabbed, or right before the transaction is pushed to the master.

    o This part of the code can be further refactored once JTA becomes an orthogonal concern and the domain logic for HA is all contained
      in one data source. Until then, this will hold the fort. For an intro to the conundrum here, see the comment in TransactionState around
      the methods tracking if the remote tranasction is initialized.

    o Added Monitors and test for HA slaves pull update behavior.

    --