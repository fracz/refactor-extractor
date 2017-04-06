commit 699ecfcf90f0edcf282b8a17e3778cacc476113d
Merge: 43817de 3ea2a32
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu May 26 14:58:24 2011 +0200

    Merge remote branch 'stloyd/validators_refactoring'

    * stloyd/validators_refactoring:
      Refactor validators constraints: - remove need for defining "getTargets()" method as 95% of validators use same one - replace abstract "Constraint::getTargets()" with one that use 95% of validators - add additional tests for "Constraint::getTargets()" method - remove unused "use" statement in Constraint\Valid