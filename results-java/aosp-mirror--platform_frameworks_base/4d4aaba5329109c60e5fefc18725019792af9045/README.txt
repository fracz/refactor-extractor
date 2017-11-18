commit 4d4aaba5329109c60e5fefc18725019792af9045
Author: Adrian Roos <roosa@google.com>
Date:   Wed Jul 26 19:17:51 2017 +0200

    AOD: Introduce night brightness bucket

    Also refactors the brightness level translation such that it can
    be done purely in config.xml.

    Bug: 63995944
    Test: mp sysuig
    Change-Id: I995082c74ba0eda7baa7879abeb34862f6989725