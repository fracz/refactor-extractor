commit e50473ec0ff19786fae997c4f9b09bca368d5bae
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Thu Sep 25 19:35:35 2014 +0200

    Refactorings and improvements on batch importer staging

    The staging framework for the ParallelBatchImporter has been
    changed to reduce time spent in the framework itself as well
    as reporing much more accurate statistics.

    - Simplified Stage to only have #add(Step), instead of input/add
    - Simplified and streamlined waiting of conditions in individual steps
      where conditions are first busy waited a little while, to then
      back off to a sleep strategy.
    - Improved accuracy of statistics of upstream/downstream idling,
      possible since better placed for measuring was found.
    - DetailedExecutionMonitor will find and print which step is the
      current most likely step to be the bottle neck in the stage.
    - Correctly and efficiently follows the contract of a step, where
      batches must be sent downstream in the order they arrived, which
      implies total ordering as a whole between steps. Previously there
      was too strict checks such that some parallelization was hindered.