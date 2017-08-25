commit fee2cde5b6651ccdf8e1da7a358fb3bc595c6146
Author: Rajesh Taneja <rajesh@moodle.com>
Date:   Tue Feb 2 10:06:25 2016 +0800

    MDL-52970 behat: Navigation step improvement

    If navigation node to expand is a link then first
    click on it and navigate to next page where it is
    expanded, and then click on link within. This is
    needed as some drivers click on link and don't open
    the navigation node which are links