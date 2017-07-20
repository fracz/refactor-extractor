commit b4f6b6fc372dee053e56f4ba89051aba6df58775
Author: mdomenjoud <mdomenjoud@octo.com>
Date:   Thu Mar 12 16:23:54 2015 +0100

    Refactor test hierarchy to split properly between Unit & Integration tests :
    * configuration with access to database loaded only with IntegrationTestCase inheritance
    * all Integration tests moved to integration directory
    * PrestaShopPHPUnit refactored as a static helper
    *ModulePrestaShopPHPUnit deleted as a duplicate of ModuleInstallUninstallTest