commit 8bc8e0b468042cc5def95b0708c014a411f714e8
Author: Mark Story <mark@mark-story.com>
Date:   Sun Jun 11 09:10:50 2017 -0400

    Make init() chainable and improve docs.

    I bumped into this during the workshops and found it annoying that
    init() couldn't be chained as I wanted to do
    `helper('Progress')->init(...)->output(...)` and couldn't.