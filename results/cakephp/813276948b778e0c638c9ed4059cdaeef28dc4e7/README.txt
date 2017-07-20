commit 813276948b778e0c638c9ed4059cdaeef28dc4e7
Author: Florian Krämer <florian.kraemer@cakedc.com>
Date:   Fri Jul 1 01:18:20 2016 +0200

    Refactoring the HtmlHelper table generation code.

    I came across #8995 and seen the code and refactored it to make it more easy to overload parts of the code and to get smaller methods that do a single task.