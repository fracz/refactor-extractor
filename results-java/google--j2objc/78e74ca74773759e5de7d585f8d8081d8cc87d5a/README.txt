commit 78e74ca74773759e5de7d585f8d8081d8cc87d5a
Author: tball <tball@google.com>
Date:   Thu Jun 30 16:12:59 2016 -0700

    Automated g4 rollback of changelist 126308839.

    *** Reason for rollback ***

    Refactored to remove jre_security dependencies from IosHttpURLConnection.

    *** Original change description ***

    Automated g4 rollback of changelist 126239776.

    *** Reason for rollback ***

    Needs refactoring to avoid adding dependency on jre_security library.

    *** Original change description ***

    Merge pull request #762 from Buggaboo/getServerCertificates

    Implementation of getServerCertificates for HttpsURLConnection

    ***

    ***

            Change on 2016/06/30 by tball <tball@google.com>

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=126355691