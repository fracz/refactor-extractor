commit b82acb31c51ca7b5629766d6438a9451ba8647d6
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Feb 18 16:54:26 2015 +0300

    PY-14979 Manually add value of the "NAME" template variable when creating new file for refactoring

    However PACKAGE_NAME variable for Python templates is still undefined
    and it seems that better to create such new files via FileTemplateUtil
    instead somehow.