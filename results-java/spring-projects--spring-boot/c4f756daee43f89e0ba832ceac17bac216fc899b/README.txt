commit c4f756daee43f89e0ba832ceac17bac216fc899b
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Mon Jan 18 13:18:03 2016 +0000

    Fix MavenSettings’ handling of profiles activated by a file

    Previously, MavenSettings used a FileProfileActivator with no
    PathTransformer. If a settings.xml file contains a file-activated
    profile this would result in an NPE within Maven. This was made worse
    by the NPE not being included in the resulting failure message which
    hampered diagnosis of the problem.

    This commit updates MavenSettings to configure its FileProfileActivator
    with a PathTransformer. It also improves the failure message that’s
    created from any problems that are reported by Maven while determining
    the active profiles to include a problem’s exception if it has one.

    Closes gh-4826