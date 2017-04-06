commit 75b4f408e05fc19de6fce775132239f1e22c99ac
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Wed Apr 5 14:43:01 2017 +0200

    Refactor InternalEngine's index/delete flow for better clarity (#23711)

    The InternalEngine Index/Delete methods (plus satellites like version loading from Lucene) have accumulated some cruft over the years making it hard to clearly the code flows for various use cases (primary indexing/recovery/replicas etc). This PR refactors those methods for better readability. The methods are broken up into smaller sub methods, albeit at the price of less code I reused.

    To support the refactoring I have considerably beefed up the versioning tests.

    This PR is a spin-off from #23543 , which made it clear this is needed.