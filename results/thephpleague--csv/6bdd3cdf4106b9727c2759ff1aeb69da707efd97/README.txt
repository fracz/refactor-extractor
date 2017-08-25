commit 6bdd3cdf4106b9727c2759ff1aeb69da707efd97
Author: Ignace Nyamagana Butera <nyamsprod@gmail.com>
Date:   Tue Mar 7 09:52:20 2017 +0100

    Improve Codebase

    - AbstractCsv::destruct method uses array_walk_recursive
    - StreamIterator uses the ValidatorTrait
    - AbstractCsv::fpassthru improved
    - Reader internal code improved