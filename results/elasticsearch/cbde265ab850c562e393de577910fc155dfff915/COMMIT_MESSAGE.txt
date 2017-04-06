commit cbde265ab850c562e393de577910fc155dfff915
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Jul 26 17:12:34 2011 +0300

    improvement to string splitting caused fields= on get to return the source back, fix it and also optimize this case when using realtime get, closes #1164.