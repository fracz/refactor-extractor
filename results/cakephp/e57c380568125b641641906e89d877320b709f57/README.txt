commit e57c380568125b641641906e89d877320b709f57
Author: Mark Story <mark@mark-story.com>
Date:   Mon Dec 15 21:59:00 2014 -0500

    Add View::cache()

    This new method will allow arbitrary blocks of view code to be cached.
    This allows some refactoring of element() and will also power cached
    cells. Lastly, it gives developers a flexible way to incrementally add
    caching to their responses if they have expensive to build page
    fragments.

    I've repurposed View::_elementCache() as its original purpose was a bit
    silly. I've also removed the elementCacheSettings property which should
    have never existed/been public in the first place.