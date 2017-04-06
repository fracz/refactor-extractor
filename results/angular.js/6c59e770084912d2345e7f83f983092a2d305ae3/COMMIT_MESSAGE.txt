commit 6c59e770084912d2345e7f83f983092a2d305ae3
Author: Boris Serdyuk <just-boris@hotmail.com>
Date:   Sat Sep 21 12:38:09 2013 +0400

    refactor(angular.toJson): use charAt instead of regexp

    Provides a performance improvement when serializing to JSON strings.

    Closes #4093