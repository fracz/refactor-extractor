commit 3ce5b4b02b98c823de7fcc897de60412d2211e7e
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Jul 3 19:26:25 2017 +0300

    push: improve default selection highlighting and expansion

    * highlight current or first if no preselected repos in the context;
    * automatically expand only checked repos after loading;
    * IDEA-175063 - Change push dialog strategy;