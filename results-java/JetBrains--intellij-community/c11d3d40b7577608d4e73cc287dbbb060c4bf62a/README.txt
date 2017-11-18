commit c11d3d40b7577608d4e73cc287dbbb060c4bf62a
Author: Raphael Moll <raphael@google.com>
Date:   Thu Jun 20 14:08:10 2013 +0200

    [cherry-picked from AOSP and slightly refactored by yole]

    Change StatisticsService extension point.

    Change the StatisticsService extension point to a bean/keyed EP
    which key is set by the <statistics> element from AppInfo.xml

    StatisticsService EP can now override the text used
    in the statistics notification/configuration.