commit 91819d932d1db63bd706d48bde94a2c80558628f
Author: jfarcand <jfarcand@apache.org>
Date:   Wed Feb 2 09:36:27 2011 -0500

    Do not use the polling value for setting keep alive. Also use a non Connections pool when set to false to improve performance