commit 075843c263ef9e638eceffa93aa093d36d31b1a2
Author: Jonathan Gerrish <jonathan@indiekid.org>
Date:   Tue Sep 26 12:08:49 2017 -0700

    Fix ShadowResourcesTest.getText_withLayoutId()

    Robolectric behaviour deviates from the platform, so change the
    endsWith() check to match what the platform does.

    This can be changed to equalTo() during the resources refactor if we fix
    it to match the framework.