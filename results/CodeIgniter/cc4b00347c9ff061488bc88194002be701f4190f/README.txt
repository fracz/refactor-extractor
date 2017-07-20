commit cc4b00347c9ff061488bc88194002be701f4190f
Author: Andrey Andreev <narf@bofh.bg>
Date:   Thu Nov 29 17:21:43 2012 +0200

    Added CI_Output::get_header()

    (an improved version of PR #645)

    Also fixed get_content_type() to only return the MIME value and created
    Output library unit tests for both of these methods.