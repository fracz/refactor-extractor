commit de0d7944dd4619858374515478389718ee0e399f
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Mon Jun 6 16:53:06 2011 +0200

    Re-added static container to prevent reopening the log on every instantiation. This approach needs to be changed to use a DIC but that is a larger refactoring