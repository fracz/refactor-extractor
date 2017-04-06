commit a1df3c637c14af5572e9e0bed42a4c38e7770239
Author: kimchy <kimchy@gmail.com>
Date:   Tue Dec 21 13:02:15 2010 +0200

    improve logic of when to load fields from source, only if they actually have mappings, otherwise, ignore them (as was the previous behavior)