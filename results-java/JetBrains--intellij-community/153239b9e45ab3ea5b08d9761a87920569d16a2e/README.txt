commit 153239b9e45ab3ea5b08d9761a87920569d16a2e
Author: Sergey Savenko <sergey.savenko@jetbrains.com>
Date:   Tue Oct 14 18:41:19 2014 +0400

    DBE: improve table editor painting performance

    1) Compute cell renderer's preferred size manually;
    2) Only text that can become visible gets set to EditorImpl.