commit ffca91dda02a23e7ff960e9b90a8dc7e9daec31d
Author: Shay Banon <kimchy@gmail.com>
Date:   Wed Dec 21 04:27:28 2011 +0200

    improve detection of which logging library to use, use slf4j if its the log4j jar file used (does not have setLevel method)