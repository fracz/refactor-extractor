commit b2a1e5559861be443082644817284df187d7218f
Author: Matthew Sibigtroth <matthew.sibigtroth@gmail.com>
Date:   Mon Jan 12 14:47:09 2015 -0800

    notification ux improvements

    Currently if two or more beacons exist, two notifications are created
    plus a summary notification. The creation order was backwards so that
    the two notifications would be created and then the summary. This made
    two notification appear and then collapse into the summary. Here we
    make the summary first, so the two notifications are not shown first.