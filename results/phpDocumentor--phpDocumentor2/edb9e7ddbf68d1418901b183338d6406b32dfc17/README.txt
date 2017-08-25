commit edb9e7ddbf68d1418901b183338d6406b32dfc17
Author: Mike van Riel <me@mikevanriel.com>
Date:   Thu Aug 21 17:48:05 2014 +0200

    Refactored Transformer and TransformCommand

    The goal of this refactoring was:

    1. To move all output logic (logging) from the Transformer back to
       the TransformCommand using events.
    2. To allow writers to initialize themselves or the ProjectDescriptor
       before all transformations are executed. This allows writers to set
       up globals and other state using the ProjectDescriptor that other
       writers could use. An example is the upcoming Search writer that can
       inject a partial to show a search box tailored to a specific search
       type.
    3. To move behaviours out of the Transformer as to make it more lean
       and loosen dependencies.
    4. To clean up the code of the Transformer