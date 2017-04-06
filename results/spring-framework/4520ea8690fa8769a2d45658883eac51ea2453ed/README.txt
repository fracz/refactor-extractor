commit 4520ea8690fa8769a2d45658883eac51ea2453ed
Author: Chris Beams <cbeams@vmware.com>
Date:   Wed May 18 08:53:46 2011 +0000

    Revert #processConfigBeanDefinitions to 3.0.x API

    Revert signature of
    ConfigurationClassPostProcessor#processConfigBeanDefinitions to its form
    found in the 3.0.x line.  Refactorings made during 3.1 development
    caused otherwise package-private types such as
    ConfigurationClassBeanDefinitionReader to escape through this public
    method, causing issues for STS as well as being a general design issue.

    Upon review, the refactorings could easily be backed out in favor of a
    simpler approach, and this has been done.

    This also means that ConfigurationClassBeanDefinitionReader can return
    to package-private visibility, and this change has been made as well.

    Issue: SPR-8200