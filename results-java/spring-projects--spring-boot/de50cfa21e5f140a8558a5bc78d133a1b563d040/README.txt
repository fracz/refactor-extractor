commit de50cfa21e5f140a8558a5bc78d133a1b563d040
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Sun Jan 22 23:14:23 2017 -0800

    Make OnClassCondition an AutoConfigurationImportFilter

    Update OnClassCondition to implement AutoConfigurationImportFilter so
    that auto-configuration candidates can be filtered early. The
    optimization helps to improve application startup time by reducing
    the number of classes that are loaded.

    See gh-7573