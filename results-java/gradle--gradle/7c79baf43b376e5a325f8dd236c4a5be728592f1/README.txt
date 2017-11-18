commit 7c79baf43b376e5a325f8dd236c4a5be728592f1
Author: Eric Wendelin <eric@gradle.com>
Date:   Mon May 15 21:47:35 2017 -0700

    Remove unnecessary fields from progress events

    This change removes the category from progress and progress
    complete events. They are never used in logging because the
    category from the start event is used for generated log messages.

    Similarly, the timestamp is removed from progress events as it's
    never used.

    Finally, the description for progress complete events is never
    used.

    All of these reduce the serialization burden of a now much-larger
    number of progress events, resulting in improved performance.