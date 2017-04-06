commit 201fc06f8d7e28a0940365314a786ac6230f7ace
Author: Igor Motov <igor@motovs.org>
Date:   Wed Mar 23 17:12:30 2016 -0400

    Fix TaskId#isSet to return true when id is set and not other way around

    During refactoring the name was changed, but the logic wasn't. This commit fixes the logic to match the name.