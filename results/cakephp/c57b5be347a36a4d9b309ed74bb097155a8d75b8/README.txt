commit c57b5be347a36a4d9b309ed74bb097155a8d75b8
Author: Mark Story <mark@mark-story.com>
Date:   Wed Jun 22 09:13:08 2016 -0400

    Only propagate the connection on automodels.

    Do some refactoring in BelongsToMany::junction() to reduce nesting
    depth.

    Refs #9018