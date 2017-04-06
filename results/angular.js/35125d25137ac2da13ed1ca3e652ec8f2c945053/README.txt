commit 35125d25137ac2da13ed1ca3e652ec8f2c945053
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Mar 27 12:44:37 2012 -0700

    refactor(toJson): use native JSON.stringify

    Instead of using our custom serializer we now use the native one and
    use the replacer function to customize the serialization to preserve
    most of the previous behavior (ignore $ and $$ properties as well
    as window, document and scope instances).