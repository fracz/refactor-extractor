commit 31162bcf061cd58a5db628f51f3da5d7d43b7829
Author: Luis Fernando Pino Duque <lpino@google.com>
Date:   Wed Apr 20 09:26:20 2016 +0000

    Delete the defaultMultipleValue field from options and refactor the logic for retrieving
    the default values of options.

    The field defaultMultipleValue was introduced in commit 51a491b89a9cd5f15c9a093a5693bc37e696e6e1 to allow defining a
    default value for options that set allowMultiple. However due to the limitations of
    the optionsParser end up being not useful since we cannot guarantee that an option
    that allows multiple has a converter that returns a list of values.
    Thus this CL deletes code that may confuse even more and clarifies the mechanism
    that the options currently use to obtain their default values.

    --
    MOS_MIGRATED_REVID=120317261