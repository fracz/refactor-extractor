commit 0208c0ab439a2118c1e2af5473fbb63c1336c368
Author: Oscar Rydhe <oscar.rydhe@sonyericsson.com>
Date:   Fri Jan 14 09:23:48 2011 +0100

    Improved conversion accuracy of exif geotag data

    Changed degrees and minutes of geotag data into double to avoid
    data loss during cast.

    Also improved error handling if geotag data can't be parsed.

    Change-Id: I864843c7fc699fe81e6acba801fe46d10a01925b