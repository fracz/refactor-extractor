commit 06728cba3b29c7c254360b2d4fd2c514ebfe1635
Author: mgp25 <me@mgp25.com>
Date:   Sat Apr 1 14:55:21 2017 +0200

    Multiple fixes and improvements

    Updated signature functions and splitted generateSignature into 2
    different functions, one used for calculating the signature and the
    other one to generate the signed string. This will be useful when using
    it with GET requests.

    Updated Constants, renamed VERSION to IG_VERSION, so we dont get
    confused with other constants. also added some Facebook constants used
    for some functions and MQTT. Using 10.15.0 data.

    Fixed some responses where a response value was missing