commit 55d77354c753ca4173bdaf0e42d87a3aa419ac5a
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Wed Aug 17 12:20:30 2011 +0400

    GitComplexProcess refactoring

    1. Make not abstract, pass an Operation to the constructor instead of extending the class.
    2. Pre-create all preparation and completion operations for better readability in run().