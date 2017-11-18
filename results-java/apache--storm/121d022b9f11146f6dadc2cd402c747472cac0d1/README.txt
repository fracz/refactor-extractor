commit 121d022b9f11146f6dadc2cd402c747472cac0d1
Author: Boyang Jerry Peng <jerrypeng@yahoo-inc.com>
Date:   Tue Oct 6 22:23:18 2015 -0500

    [STORM-893] - Resource Aware Scheduler implementation.
    [STORM-894] - Basic Resource Aware Scheduling implementation.

    Added functionality for users to limit the amount of memory resources allocated to a worker (JVM) process when scheduling with resource aware scheduler. This allows users to potentially spread executors more evenly across workers.
    Also refactored some code