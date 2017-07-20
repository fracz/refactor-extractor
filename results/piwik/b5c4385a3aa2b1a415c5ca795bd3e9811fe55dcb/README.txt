commit b5c4385a3aa2b1a415c5ca795bd3e9811fe55dcb
Author: Stefan Giehl <stefan@piwik.org>
Date:   Mon Nov 21 00:44:49 2016 +0100

    Fix goal metric processing for device type / model / brand (#10873)

    * always persist visitor property if it's dimension implements `onAnyGoalConversion` event

    * small code improvements

    * adds some tests to prove converted goals are processed for device type / model / brand

    * update tests