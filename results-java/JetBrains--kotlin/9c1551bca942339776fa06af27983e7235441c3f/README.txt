commit 9c1551bca942339776fa06af27983e7235441c3f
Author: Mikhail Glukhikh <Mikhail.Glukhikh@jetbrains.com>
Date:   Tue Mar 24 17:03:01 2015 +0300

    Implementation of smart casts for public / protected immutable properties that are not open and used in the same module.
    DataFlowValueFactory and its environment refactoring: containing declaration is added into factory functions
    as an argument and used to determine identifier stability. A few minor fixes. #KT-5907 Fixed. #KT-4450 Fixed. #KT-4409 Fixed.

    New tests for KT-4409, KT-4450, KT-5907 (public and protected value properties used from the same module or not,
    open properties, variable properties, delegated properties, properties with non-default getter).
    Public val test and KT-362 test changed accordingly.