commit 621643a5c3790191d553968c017dfed7df3683f0
Author: Ryan Ernst <ryan@iernst.net>
Date:   Tue Jan 17 13:38:32 2017 -0800

    Build: Only add ASL license to pom for elasticsearch project (#22664)

    The extra plugins that may be attached to the elasticsearch build
    contain their own license. In the past, the ASL license elasticsearch
    uses was avoided by specially checking for the gradle project prefix of
    `:x-plugins`. However, since refactoring to the elasticsearch-extra dir
    structure, this mechanism was broken. This change fixes the pom license
    adding to only be applied to projects that fall under the root project
    (ie elasticsearch).