commit a9c90d0e4af85f8ef131fe089456a5416233a630
Author: Trustin Lee <trustin@gmail.com>
Date:   Thu Feb 18 13:00:31 2010 +0000

    Reverted back the changes about direct buffer pool - it doesn't seem to improve the performance as much as additional memory consumption and the latest one often led to OOME during testing