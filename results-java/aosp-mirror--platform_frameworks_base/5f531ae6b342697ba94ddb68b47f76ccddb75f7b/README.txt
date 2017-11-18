commit 5f531ae6b342697ba94ddb68b47f76ccddb75f7b
Author: Steve Howard <showard@google.com>
Date:   Thu Aug 5 17:14:53 2010 -0700

    Slight improvement (hopefully) to orientation sensing.

    Since orientation sensing has been an issue for numerous users, I
    decided a spend a little time experimenting with some possible
    improvements.  I've settled on a couple major changes:

    * Perform all lowpass filtering in spherical coordinates, not
      cartesian.  Since the rotations are what we're really concerned
      with, this makes more sense and gives more consistent results.

    * Introduce a system of tracking "distrust" in the current data, based
      on external acceleration and on tilt.  The basic idea is after a
      signal of unreliable data -- repeated acceleration or
      nearly-horizontal tilt -- we wait for things to "stabilize" for some
      number of ticks before we start trusting the data again.  This is an
      extension of the basic lowpass filtering.  One simple example is
      after the phone is picked up off a table, we ignore the first few
      readings.  Another example is while the phone is under external
      acceleration for a while (i.e. in a car mount on a rough road), if a
      single "good" reading comes in, we distrust it, under the assumption
      that it was probably just a lucky reading (i.e. the magnitude
      happened to be close to that of gravity by chance).

    These changes have allowed me to relax other constraints, such as the
    filtering time constants, the maximum deviation from gravity, and the
    max tilt before we start distrusting data.

    The net effect is that orientation changes happen more quickly and can
    happen under a wider variety of conditions, but false changes due to
    tilt and acceleration are still avoided well.  I think the improvement
    is subtle, but it's the best I've come up with in my limited time.

    I've also included some refactoring and additonal comments to try and
    further clarify the (somewhat twisted) logic.

    Change-Id: I34c7297bd2061fae8317ffefd32a85c7538a3efb