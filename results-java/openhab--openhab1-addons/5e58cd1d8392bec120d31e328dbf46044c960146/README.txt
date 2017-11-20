commit 5e58cd1d8392bec120d31e328dbf46044c960146
Author: Markus Eckhardt <Markinus@users.noreply.github.com>
Date:   Wed Jan 18 18:40:27 2017 +0100

    [km200] Bug fixes; install/config in OH2 (#5000)

    * Fixed suboptimal if statement

    * Changed binding configuration to be OH2 compatible

    * Removed all Require-Bundle and version dependencies

    * Changed the setpoint names back to a dynamic version. There are much more setpoint variants as expected.

    * Changed the projects name to be able to import the OH2 skeleton

    * Added some more log outputs

    * Added to OH2 and enabled binding configuration from PaperUI

    * Added a workaround for HTTP 500 Errors from device
    Changed the floatValue conversion

    * Some code cleaning and improvements

    * Changed the data conversion for BigDecimal