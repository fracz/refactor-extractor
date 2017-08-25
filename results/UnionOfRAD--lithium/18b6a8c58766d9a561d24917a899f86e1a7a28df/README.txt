commit 18b6a8c58766d9a561d24917a899f86e1a7a28df
Author: Nate Abele <nate.abele@gmail.com>
Date:   Thu Jul 14 19:10:49 2011 -0400

    Refactoring MongoDB changeset calculation for update operations, to improve efficiency. Renaming `keys` config field in `\data\model\Relationship` to `key`, to better reflect default use case.