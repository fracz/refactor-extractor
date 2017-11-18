commit ac42df773fdcd4ecbfd4de68455dc989b3d7adb2
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Thu Aug 30 19:25:34 2012 -0700

    Complete refactoring of the exception handling in the disk storage layer of Titan. Added transaction retries for temporary storage failures. Closes #32.