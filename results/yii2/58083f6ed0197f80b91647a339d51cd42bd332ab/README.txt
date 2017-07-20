commit 58083f6ed0197f80b91647a339d51cd42bd332ab
Author: Michael Bodnarchuk <davert@ukr.net>
Date:   Wed Jul 31 22:39:13 2013 +0300

    A simple patch to get AspectMock and Go Aop working with Yii2

    This is a very tiny patch that changes nothing in code logic, but is required to get [AspectMock](https://github.com/Codeception/AspectMock) and Go Aop working with Yii2.

    Go Aop is processing all `include` and `require` directives, replacing them with its filters. Unfortunately it doesn't play well with one-liners. So I had to break the code into few lines to get that working.

    I was trying to fix this issue in [Go Aop](https://github.com/lisachenko/go-aop-php/pull/78) but looks like, the only option is to fix that in Yii2.

    AspectMock can dramaticly improve unit testing in Yii2, and I plan to do a blogpost with tutorial about it.