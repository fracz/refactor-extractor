commit 94692181a5e7be9436d79d121c3d0006874a518f
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Mon Mar 5 13:17:57 2012 +0100

    Fix for transaction interceptor after a recent refactoring which made the same instance to be reused so that the diff became bigger and bigger for multiple consecutive transactions