commit 0c950e6ddbffbaf9e5b40e7c7306af72a3c238f3
Author: Nate Abele <nate.abele@gmail.com>
Date:   Mon Mar 29 02:05:00 2010 -0400

    Implementing persistent route parameters, refactored `\net\http\Router::connect()` to handle parameters more flexibly, added `Router::process()` as convenience method for handling `Request` objects.