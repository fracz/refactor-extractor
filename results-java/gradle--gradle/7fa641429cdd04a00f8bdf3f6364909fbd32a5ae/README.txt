commit 7fa641429cdd04a00f8bdf3f6364909fbd32a5ae
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Jun 27 01:58:35 2013 +0200

    Message for incubating execution mode is not shown if the mode is explicitly configured (e.g. gradle.properties).

    1. The use case is that organisations may roll out incubating mode to all projects and don't want to confuse the end users (e.g. fear about the features that may be perceived incomplete, confusing request for feedback in the incubating message). So, if the incubating mode is explicitly configured in the gradle properties, we assume that the user knows what he is doing and need not be reminded about the incubating nature of the feature. (while writing this commit message it just occurred to me that this should be in the spec)
    2. The implementation is not quite pretty, looking for suggestions on how to improve it.