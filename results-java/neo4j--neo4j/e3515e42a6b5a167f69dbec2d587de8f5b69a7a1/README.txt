commit e3515e42a6b5a167f69dbec2d587de8f5b69a7a1
Author: Mark Needham <mark.needham@neotechnology.com>
Date:   Mon Jan 11 16:04:37 2016 +0000

    Retry logic for transactions that time out.

    Fixes a race which could lead to an infinite loop after
    and initial timeout and retry.

    Includes a number of small improvements:
    * Only create one future per operation id.
    * Extract CommittingTransactionsRegistry from
      ReplicatedTransactionStateMachine.
    * Introduce explicit CommittingTransaction instead of Future<Long>.
    * Removed lock session checking from ReplicatedTransactionCommitProcess
      because the checks in ReplicatedTransactionStateMachine are
      sufficient.
    * Use KernelException variant of TransactionFailureException since
      all the exceptions thrown in this code are internal and not exposed
      to the user.
    * Restore interrupt status of current thread after catching
      InterruptedException.