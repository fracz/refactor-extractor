commit bae2f3927ac07561b8707b484fe76d2f893bb784
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Thu Jan 19 13:17:46 2012 +0200

    Introduced and refactored around an extract tx interval from datasource method in MasterUtil
    Used that to introduce a copy transactions from master feature to recover from missing or damaged logical logs
    Comments, comments, comments