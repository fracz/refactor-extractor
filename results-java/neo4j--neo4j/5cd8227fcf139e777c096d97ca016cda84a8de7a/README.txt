commit 5cd8227fcf139e777c096d97ca016cda84a8de7a
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Thu Apr 25 13:39:06 2013 +0200

    Refactors TxState to hold temporary state instead of writing to PersistenceManager.

    Makes updating the schema with constraints invalidate the schema state.
      This is the primary driver for the refactoring, there are now appropriate hooks for this.

    Allows SchemaState to contain state without having to write to WriteTransaction until pre-commit.
      By making the lowest level TransactionContext invoke TxManager.commit() in its commit() method,
      all TransactionContexts can do things both pre and post commit (by doing things before or after
      invoking the delegate. This allows us to defer generating ids and records for things that change
      in a transaction until we know we are about to commit the transaction.

    Restructures then Operations interfaces in the kernel API to a clearer, more useful separation.
      Separating read from write operations, and separating operations on the schema from operations
      that change entities, and from operations about key-generation (property keys, labels, and
      relationship types). Changes to schema state is also split out into its own Operations
      interface, but without a Read/Write split, since these operations are not reads/writes against
      the stored data, and as such not transactional operations in the same way.
      These Operations interfaces come together in a few utility interfaces, grouping the operations
      along the two axis that they are divided by before coming together into StatementContext.