commit a6959226ec30d5f960b80a764487d2a22563b234
Author: Tglman <tglman@tglman.com>
Date:   Thu Jun 25 13:44:23 2015 +0100

    refactored shutdown flow for terminate before all threads, clean all running db, and at the end close the storages, issue #4441