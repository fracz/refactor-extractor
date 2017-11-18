commit d3d1c8fe8036a13844365519c6a1100641ce8ac8
Author: Anna Kozlova <anna.kozlova@jetbrains.com>
Date:   Wed Jun 14 17:54:14 2017 +0300

    extract method: throw prepare failed in case of chained refactorings

    otherwise there is no way to get explanation why refactoring was not applicable