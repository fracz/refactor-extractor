commit 98d5f04f91580bf8b517c17132eeb8d42008b61d
Author: Yigit Boyar <yboyar@google.com>
Date:   Tue Nov 18 15:47:56 2014 -0800

    Improve GridLayout's weight calculations

    This CL improves the method by which excess space is distributed in GridLayout.
    Previously, GridLayout would assume weights were arranged in a 'line' and
    sum the weights in the assumed line to figure out the proportional allocation
    to each view. The system involved running GridLayout's internal constraint
    solver twice.

    Behavior was unspecified (and surprising) when weights appeared in views
    that were not linked together linearly, typically leaving the last view
    in each axis with more space than expected (in GridLayout's Bellman-Ford
    constraint solver, remaining space goes to the last span of the axis).

    This CL changes the weight distribution mechanism to effectively integrate it
    with the Bellman-Ford constraint resolution algorithm. It does this
    by returning a boolean value from the constraint solver saying whether or
    not the constraints could be solved and then using a binary chop to find
    a maximum amount of space that can be distributed without violating the
    constraints.

    This implementation runs the solver log(<axis size> * <number of Views>)
    times until finding the maximum amount of space that can be distributed according
    to the weights without causing a contradiction. We expect the cost of this
    variation to be around a factor of 10 worse than the previous implementation
    but to provide a simple and general definition of space distribution via
    weights that will be open to many future optimizations.

    As a side effect, this CL also fixes a bug in GridLayout where remaining space
    was distributed only along the major axis.

    Bug: 17485996
    Change-Id: I120f39e95e90b5b35072ef8a6c348ec541aae42a