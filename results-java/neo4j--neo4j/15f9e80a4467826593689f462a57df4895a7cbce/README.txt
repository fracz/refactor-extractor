commit 15f9e80a4467826593689f462a57df4895a7cbce
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Wed Jul 9 13:35:29 2014 +0200

    Fix: several bugs in all Thresholds for log pruning after the refactor which has separated
     log pruning strategy and thresholds

    Added method init() to Threshold interface to notify when a new pruning check begins

    Add tests for FileSizeThreshold, FileCountThreshold, TransactionTimespanThreshold