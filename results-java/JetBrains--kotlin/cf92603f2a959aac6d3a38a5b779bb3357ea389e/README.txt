commit cf92603f2a959aac6d3a38a5b779bb3357ea389e
Author: Alex Tkachman <alex.tkachman@gmail.com>
Date:   Thu Sep 13 08:33:07 2012 +0300

    micro refactoring
    - PsiCodegenPredictor moved to binding package
    - CodegenUtil.findSuperCall method moved to CodegenBinding
    = PsiCodegenPredictor unneeded bindingTrace parameter replaced by bindingContext