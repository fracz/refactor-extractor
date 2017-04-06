commit 813117da31bfc8731e8af26336915e29878b4294
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Wed Feb 1 14:07:26 2017 +0100

    refactor($injector): remove the Chrome stringification hack

    The Chrome stringification hack added in afcedff34c8a44dda0d558d9d6337962f5f03d7b
    is no longer needed. I verified that both of the commented out tests pass
    on Chrome 56.