commit 20ca194eefe843d6e392e88af6ae324e94db29d9
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Wed Feb 29 19:21:10 2012 +0400

    refactor TypeVariableResolver

    * simplified and unified
    * better diagnostics (if type variabe is not found, exception message contains reference)