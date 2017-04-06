commit 47b075017145dffdd875d2007127b37e868497ee
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Jul 26 16:39:36 2011 +0300

    improve lock logic when recovering to reduce chance of throwable slipping and causing no release of lock