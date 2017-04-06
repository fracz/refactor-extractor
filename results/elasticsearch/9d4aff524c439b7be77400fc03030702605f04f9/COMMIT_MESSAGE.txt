commit 9d4aff524c439b7be77400fc03030702605f04f9
Author: Ryan Ernst <ryan@iernst.net>
Date:   Sun Mar 12 08:20:32 2017 -0700

    Build: Split output for jrunscript to stdout and stderr (#23553)

    While trying to improve the failure output in #23547, the stderr was
    also captured from jrunscript. This was under the assumption that stderr
    is only written to in case of an error. However, with java 9, when
    JAVA_TOOL_OPTIONS are set, they are output to stderr. And our CI sets
    JAVA_TOOL_OPTIONS for some reason. This commit fixes the jrunscript call
    to use a separate buffer for stderr.