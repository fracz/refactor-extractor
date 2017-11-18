commit b782fd45f7046a94a617ec8aec76ee5513555924
Author: Marcel Hlopko <hlopko@google.com>
Date:   Wed Nov 16 15:48:25 2016 +0000

    Simplify Crosstool Build Variables context

    This cl merges two classes used to hold build variables context into one. Those
    classes are (previously top-level) jcgd.build.lib.rules.cpp.Variables, and its
    inner class View. Both represent a collection of build variables and imo it
    makes sense to merge them to make the code simpler.

    Also, I cleaned up the build variables api to use primitive strings or instances of VariableValueBuilders, and I've hidden subclasses of VariableValues.

    Last but not least, I refactored the code to use immutable collections exclusively. That revealed that 'module_files' variable is sometimes registered twice. I want to clean this eventually ([]).

    --
    MOS_MIGRATED_REVID=139329823