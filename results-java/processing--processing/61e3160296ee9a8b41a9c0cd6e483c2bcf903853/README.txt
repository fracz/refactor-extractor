commit 61e3160296ee9a8b41a9c0cd6e483c2bcf903853
Author: Jakub Valtar <jakub.valtar@gmail.com>
Date:   Fri Apr 29 16:44:29 2016 +0200

    ECS + ASTGen: threading

    - when continuous error checking is disabled, the sketch is preprocessed
    on demand
    - disable whole infrastructure when there are java tabs
    - error checker now accepts callbacks which run after error check
    finishes
    - preprocessed sketch can be no longer obtained directly via public
    field; ASTGenerator was refactored to allow passing the preprocessed
    sketch down through the hierarchy (should be refactored later)
    - rename, go to declaration and show usage now run in background thread