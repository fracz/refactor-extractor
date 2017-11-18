commit 690575ec466b5ef6099c3a731e97d45b28c98b33
Author: tedyu <yuzhihong@gmail.com>
Date:   Thu Sep 21 13:57:54 2017 -0700

    MINOR: Follow-up improvements on top of KAFKA-5793

    Simplified the condition in Sender#failBatch()
    Added log in TransactionManager#updateLastAckedOffset()

    Author: tedyu <yuzhihong@gmail.com>

    Reviewers: Jason Gustafson <jason@confluent.io>

    Closes #3935 from tedyu/trunk