commit c41245708ffbc2f932e13bffb325cc21f7b13fc1
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Thu May 18 10:48:57 2017 +0700

    Bytecode analysis: purity inference improvement (also fixes IDEA-172989):

    1. Lambda/method reference creation is pure
    2. String concatenation is pure
    3. Constructor which only modifies own fields (calls setters, etc.) is pure
    4. Exception creation is pure
    5. A few hardcoded native methods