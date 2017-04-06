commit 865ee545583e627950fedaf0c3387f8fd072c44e
Author: Xavier De Cock <xdecock@gmail.com>
Date:   Thu Mar 10 18:24:33 2011 +0100

    Performance improvement on Router to avoid array_*

    Changes on the Router to avoid array_diff, array_keys and other inneficient array_ functions.

    This is a "recommit" to respect Symfony Contribution rules