commit 36d02b6c798bb1acc790967fa08434174b81126e
Author: epriestley <git@epriestley.com>
Date:   Thu Oct 4 09:30:32 2012 -0700

    Fix a use of $method in Conduit which is not available in scope

    Summary:
    This got refactored at some point and lost access to $method. Also make the error a little more helpful.

    See https://groups.google.com/forum/?fromgroups=#!topic/phabricator-dev/05voYIPV7uU

    Test Plan:
      $ arc list --conduit-uri=http://local.aphront.com:8080/
      Exception
      ERR-CONDUIT-CORE: Invalid parameter information was passed to method 'conduit.connect', could not decode JSON serialization. Data: xxx{"client":"arc","clientVersion":5,"clientDescription":"orbital:\/INSECURE\/devtools\/arcanist\/bin\/..\/scripts\/arcanist.php list --conduit-uri=http:\/\/local.aphront.com:8080\/","user":"epriestley","host":"http:\/\/local.aphront.com:8080\/api\/","authToken":1349367823,"authSignature":"54bc136589c076ea06f8e5fb77c76ea7d57aec5b"}
      (Run with --trace for a full exception trace.)

    Reviewers: vrana, btrahan

    Reviewed By: vrana

    CC: aran

    Differential Revision: https://secure.phabricator.com/D3622