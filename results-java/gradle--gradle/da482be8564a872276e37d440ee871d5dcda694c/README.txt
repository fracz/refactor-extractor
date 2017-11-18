commit da482be8564a872276e37d440ee871d5dcda694c
Author: Adam Murdoch <adam.murdoch@gradleware.com>
Date:   Tue Oct 14 17:38:31 2014 +1100

    Changed excluded task filtering so that it does not always need to configure all projects when using an unqualified task name. One implication of this change is that the task instances are no longer required to do this kind of filtering.

    Still configures all projects if no exact match can be found after configuring the root and default projects, to deal with camel case matching.

    Could be further improved if the set of tasks to run were selected prior to creating the excluded task filters and then populating the task graph.