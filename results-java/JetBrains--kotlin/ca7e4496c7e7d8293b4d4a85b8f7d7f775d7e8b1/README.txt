commit ca7e4496c7e7d8293b4d4a85b8f7d7f775d7e8b1
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu Apr 21 18:39:05 2016 +0300

    Refactor and improve class literal type checking code

    Infer something sensible instead of error types when an error is reported, such
    as absence of a type argument for Array or presence of type arguments for other
    types