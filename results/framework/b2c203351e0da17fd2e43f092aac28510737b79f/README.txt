commit b2c203351e0da17fd2e43f092aac28510737b79f
Author: Taylor Otwell <taylorotwell@gmail.com>
Date:   Mon Apr 15 09:20:36 2013 -0500

    Made several improvements.

    +- `dropColumn` now supports dynamic argument lists.
    +- Pass `route` and `request` to Closure based controller before
    filters.
    +- Added `Auth::basicStateless` method for easier API integration with
    basic auth.