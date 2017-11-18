commit 8072497ce24062c78b48122495349179508d2d32
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Thu Sep 15 16:32:17 2016 +0200

    Extract body extraction logic in w.r.f

    This commit refactors the response body extraction logic into a separate
    function: BodyExtractor. Standard populators can be found in
    BodyExtractors.