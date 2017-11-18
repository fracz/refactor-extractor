commit f1a13e00b2c478f1ece4e09e3f0aefa83178320f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Sep 19 16:14:02 2014 +0200

    Component replacements - ensured that we can module-replace versions that are forced. Currently, there is a case when 'forced' version is 'conflict-resolved'. We have different story in the backlog to improve the selection reason for the module-replaced components. For now, module-replaced components are considered 'conflict resolved' which essentially is correct.

    +review REVIEW-5136