commit 4f4b98eeb5eb322d7423f72fb78c80d18878313e
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Mon Jul 7 02:14:08 2014 +0200

    If a dimension only implements a getName() method it should extend Dimension and not VisitDimension which is more "correct" and improves performance since we do not have to check whether they are installed and whether they implement certain methods