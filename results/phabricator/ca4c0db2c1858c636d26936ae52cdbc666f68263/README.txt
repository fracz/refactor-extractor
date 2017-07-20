commit ca4c0db2c1858c636d26936ae52cdbc666f68263
Author: epriestley <git@epriestley.com>
Date:   Thu Mar 10 17:33:45 2016 -0800

    Add a key to improve Diffusion's cache fill history query

    Summary:
    Ref T10560. I don't fully understand what MySQL is doing here, but it looks like this key improves the problematic dataset in practice.

    (It makes sense that this key helps, I'm just not sure why the two separate keys and the UNION ALL are so bad.)

    This key isn't hugely expensive to add, so we can try it and see if there are still issues.

    Test Plan: Ran `bin/storage adjust`, saw key added to table. Used `SHOW CREATE TABLE ...` to verify the key exists. Used `EXPLAIN SELECT ...` to make sure MySQL actually uses it.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10560

    Differential Revision: https://secure.phabricator.com/D15460