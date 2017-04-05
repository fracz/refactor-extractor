commit ee96098eadf9857dec065ec63f0ddf0724549b39
Author: Karel Maesen <karel@geovise.com>
Date:   Sat Jan 14 17:55:27 2012 +0100

    HHH-6509 Removes obsolete code

    This removes the HBSpatial static bootstrap class and the old SPI
    mechanism. Both had become obsolete because of how spatial now
    integrates with core.

    Also reorganizes the packages to have all JTS extensions and utilities
    in a jts package.