commit edb6a3c0b9b290c81387f4f9c5fa7fb34ed65a40
Author: Jairo Ushiña <jago86@gmail.com>
Date:   Wed Aug 16 15:53:44 2017 -0500

    Fix test failure (#502)

    * Fix test failure

    Selection of files ($fileSelection->selectedFiles())  seems that in some environments generates an unordered array given a false positive. Sort the arrays fixes the problem.

    * Test refactorization for array sorting and comparing