commit 4c22364e12268961aac3ba0f2a4b60a066a16bcd
Author: Ted Wood <ted@thedigitalorchard.ca>
Date:   Sat Jan 5 16:50:31 2013 -0800

    Slight performance improvement by moving some class property initialization to the class property declarations rather than setting them in the constructor. Subclasses can always override in their own constructor if they wish to. Is there a reason why it was done the way it was done? A policy that I am not aware of?