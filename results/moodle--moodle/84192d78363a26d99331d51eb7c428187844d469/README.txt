commit 84192d78363a26d99331d51eb7c428187844d469
Author: Sam Hemelryk <sam@moodle.com>
Date:   Thu Jun 20 18:12:28 2013 +1200

    MDL-40167 dock: converted the dock JS to a YUI shifted module.

    The patch converts the dock into a YUI shifter module and at
    the same time improves several aspects of its operation.
    The features of this patch include:
    * Dock Module conversion.
    * A loader that ensures we don't include the dock JS or its
      requirements unless actually required.
    * We no longer include the dock JS for themes that don't
      enable it.
    * Blocks no longer add registration events to the page
      instead a dockable attribute is added to the html and the
      loader looks for that.
    * The dock module is properly documented and running YUIDoc
      gives good quality documentation.
    * We no longer need the dock module registration or
      subcomponent.
    * All events that can be delegated are now delegated.
    * Removed unused variables and code left over after fixes.
    * Support for docking blocks renderered using the new blocks
      render method. Better support for custom block regions.