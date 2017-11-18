commit 5ff23220752f6cd5b238331970a3d7dd06dce7a2
Author: Dmitry Jemerov <yole@jetbrains.com>
Date:   Wed Jun 30 20:49:49 2010 +0400

    improve interaction of Find Usages and global statements (PY-1167); changed PSI so that names declared in global statement are now PyTargetExpression rather than PyReferenceExpression instances