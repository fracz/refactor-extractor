commit e515492864092c16c2ecc86577a3be73ec79fa56
Author: Shanyu Zhao <shzhao@microsoft.com>
Date:   Wed May 13 12:26:53 2015 -0700

    storm-eventhubs improvement

    EventHubBolt add event formatter to format tuples into bytes
    Refactor EventHubSpoutConfig
    Add support for specifying consumer group name
    Workaround for Qpid issue that in rare cases messages cannot be received

    Signed-off-by: Shanyu Zhao <shzhao@microsoft.com>