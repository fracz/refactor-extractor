commit 55fc3a727b0d0ba18ba2852e7e630b07beee29a8
Author: Igor Motov <igor@motovs.org>
Date:   Sun May 24 15:22:06 2015 -0700

    Core: refactor upgrade API to use transport and write minimum compatible version that the index was upgraded to

    In #11072 we are adding a check that will prevent opening of old indices. However, this check doesn't take into consideration the fact that indices can be made compatible with the current version through upgrade API. In order to make compatibility check aware of the upgrade, the upgrade API should write a new setting `index.version.minimum_compatible` that will indicate the minimum compatible version of lucene this index is compatible with and `index.version.upgraded` that will indicate the version of elasticsearch that performed the upgrade.

    Closes #11095