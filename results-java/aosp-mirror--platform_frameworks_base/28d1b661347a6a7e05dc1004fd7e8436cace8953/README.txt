commit 28d1b661347a6a7e05dc1004fd7e8436cace8953
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri Apr 21 14:17:23 2017 -0700

    Address various JobScheduler API feedback.

    - New sample code.
    - Fix/improve some docs.
    - Hide JobWorkItem Parcl constructor.

    Also:

    - Add new JobWorkItem API to get the number of times it has been
    delivered.
    - Do a bit more optimization of checking if a job is ready.

    Bug: 37534393  API Review: JobInfo.Builder
    Bug: 37544057  API Review: JobServiceEngine
    Bug: 37544153  API Review: JobWorkItem

    Test: bit CtsJobSchedulerTestCases:*
    Change-Id: I66891a038fba752f45dcaed43e615fa9209b71fc