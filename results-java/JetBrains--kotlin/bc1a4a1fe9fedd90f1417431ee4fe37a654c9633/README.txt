commit bc1a4a1fe9fedd90f1417431ee4fe37a654c9633
Author: Alex Tkachman <alex.tkachman@gmail.com>
Date:   Thu Sep 20 20:19:01 2012 +0300

    massive refactoring of CodegenUtil
    - generation method names standartized on genXXX
    - many methods moved to newly created AsmUtil
    - some methods moved to CodegenContext
    - got rid of almost trivial StubCodegen