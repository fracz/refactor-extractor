commit fc2c5fe41420c2c43c6718a5a2e287afde5bdf49
Author: Lukas Reschke <lukas@owncloud.com>
Date:   Tue Feb 23 11:54:22 2016 +0100

    Add header for attachment disposition only once

    Recent refactorings have resulted in the header being added twice, this makes browsers ignore the header which removes any security gains.

    This changeset adds the header only once and adds integration tests ensuring the correct header in future.

    https://github.com/owncloud/core/issues/22577