commit 1532032f6e7834625b06c6ef5cd5cb48ab22fc8e
Author: mark_story <mark@mark-story.com>
Date:   Fri Jan 31 22:35:41 2014 -0500

    Add getByProperty and refactor association handling in EntityContext

    Add a way to get associations by property name. This allows other parts
    of the framework to operate on associations knowing only the property
    names.