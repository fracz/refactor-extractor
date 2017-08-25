commit f4e1f09a778f218f7ca739b3bdc8732f60583412
Author: Nate Abele <nate.abele@gmail.com>
Date:   Wed Jun 30 16:20:32 2010 -0400

    Fixing issue in `\net\http\Router::match()` where an empty string would be returned in certain circumstances. Adding documentation to routing classes and refactoring.