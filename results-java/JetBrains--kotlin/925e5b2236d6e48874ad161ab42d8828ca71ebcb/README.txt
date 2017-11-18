commit 925e5b2236d6e48874ad161ab42d8828ca71ebcb
Author: James Strachan <james.strachan@gmail.com>
Date:   Thu Jul 5 10:34:00 2012 +0100

    refactored the JS kotlin code so that we've a simple naming convention (*Code.kt") to refer to library code which needs to generate JS code to separate those files from the pure definitions which just refer to existing JS code already - this will help make it easier to keep the maven build and the js test cases in sync