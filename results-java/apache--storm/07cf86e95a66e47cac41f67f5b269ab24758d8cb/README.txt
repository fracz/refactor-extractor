commit 07cf86e95a66e47cac41f67f5b269ab24758d8cb
Author: Hugo Louro <hmclouro@gmail.com>
Date:   Mon Jan 30 00:02:42 2017 -0800

    STORM-2281: Running Multiple Kafka Spouts (Trident) Throws Illegal State Exception

       - Assign topic partitions to tasks running the instance of Kafka consumer that has assigned the same list of topic partitions
       - Improve logging
       - Minor code refactoring