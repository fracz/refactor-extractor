commit b0c8a6132db6ff0a5eb94586c159bf5db69aed66
Author: olly <olly@google.com>
Date:   Thu Mar 31 14:58:32 2016 -0700

    Add DataSourceFactory + implementation.

    Step 6 of the refactor involves moving the logic that's
    currently in the SourceBuilder classes in the demo app
    into new SampleSource classes in the library. These classes
    will construct video/audio/text pipelines on-demand (i.e.
    when tracks are enabled) rather than constructing them all
    up front as is currently the case in the SourceBuilder
    classes. Hence we need a way to instantiate DataSource
    instances (i.e. a DataSourceFactory ;)).
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=118722180