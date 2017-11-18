commit 5eebe9a06c6873e797b5f8c847086c35ae990ef5
Author: Dmitry Spikhalskiy <dmitry@spikhalskiy.com>
Date:   Thu Nov 3 03:22:24 2016 +0300

    Implement RoundRobin logic in RoundRobinInetAddressResolver#resolveAll

    Motivation:
    Now the ```resolveAll``` method of RoundRobinInetAddressResolver returns results without any rotation and shuffling. As a result, it doesn't force any round-robin for clients that get a result of ```resolveAll``` and use addresses from the result one by one for a connection establishing until success. This commit implements round-robin in RoundRobinInetAddressResolver#resolveAll. These improvements inspired by the discussion here: https://github.com/AsyncHttpClient/async-http-client/issues/1285

    Modifications:
    Rotate collection from internal ```resolveAll``` call by index, which is incremented every call to RoundRobinInetAddressResolver#resolveAll method.
    Random replaced by an incrementing counter, which makes code cheaper and guarantees predictable address order in tests.

    Result:
    Improved ```RoundRobinInetAddressResolver``` is compatible with clients that use ```resolveAll``` result.