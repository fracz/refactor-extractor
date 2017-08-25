commit 840b4dbeb81603ed44c30dc77be8eff98d7667c3
Author: Aleksander Machniak <alec@alec.pl>
Date:   Tue Jul 10 20:30:34 2012 +0200

    Simplified method of getting default addressbook.
    Make sure to use the same source when adding contact and checking
    if message is safe (sender is in addressbook).
    Small code improvements.