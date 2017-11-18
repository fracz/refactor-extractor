commit c51bd7ef076ec1167ab81da4ac09cad371186a11
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Wed May 8 23:30:10 2013 -0700

    Support Asynchronous Callbacks with RxJava Integration

    https://github.com/Netflix/Hystrix/issues/123

    - refactored HystrixCommand to use Observable inside the implementation to be non-blocking and callback driven
    - observe() and toObservable() public methods added
    - HystrixCollapser not yet changed