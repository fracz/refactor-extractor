commit 7e3ede288926bdfb79b1571fed74cad614935821
Author: Alan Viverette <alanv@google.com>
Date:   Wed Oct 28 16:57:57 2015 -0400

    Support for changing density of DrawableWrapper subclasses

    Includes a refactoring of DrawableWrapper classes so that the wrapper
    super class handles both drawable management and inflation. This allows
    us to immediately call through to super() in inflate and applyTheme,
    which simplifies density management.

    Bug: 25081461
    Change-Id: I8c157d340fd1f28a3a2b786c56850a67cdd452e4