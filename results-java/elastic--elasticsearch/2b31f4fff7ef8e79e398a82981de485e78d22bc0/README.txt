commit 2b31f4fff7ef8e79e398a82981de485e78d22bc0
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Nov 10 17:37:28 2015 +0100

    Mutate processor improvements

    Remove code duplications from ConfigurationUtils
    Make sure that the mutate processor doesn't use Tuple as that would require to depend on core.
    Also make sure that the MutateProcessor tests don't end up testing the factory as well.
    Make processor getters package private as they are only needed in tests.
    Add new tests to MutateProcessorFactoryTests