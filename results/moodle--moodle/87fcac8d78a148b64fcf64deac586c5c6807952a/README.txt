commit 87fcac8d78a148b64fcf64deac586c5c6807952a
Author: mjollnir_ <mjollnir_>
Date:   Mon Sep 8 13:44:36 2008 +0000

    MDL-14591 - split portfoliolib into logical parts for better readability

    Especially as some of the functions shared by base classes (portfolio plugin vs caller) were the same
    it was getting a bit unweildy to have all this in one file.