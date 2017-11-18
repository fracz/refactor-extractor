commit b247a0b3363075e22da25ade97409141da3280f5
Author: Rick Brewster <rickbrew@fb.com>
Date:   Mon Feb 1 16:40:07 2016 -0800

    Cache the result from ObjectMapper.getJsonValueMethod()

    Caching this results in significant performance wins for the Elements
    tab.

    I also experimented with a general reflection cache, e.g.
    Class<?>.getFields(), getEnumConstants(), etc. While that also seemed
    to improve things, I'm not completely confident without running a
    micro-benchmark or a sampling profiler.