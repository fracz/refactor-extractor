commit ac5e6ca4d1c7f33eb72dcc2953feb02db0657090
Author: Ruslan Kabalin <r.kabalin@lancaster.ac.uk>
Date:   Thu Jan 24 14:28:40 2013 +0000

    MDL-30637 Refactor Advanced fileds functionality

    The displaying of advanced items has been refactored. The changes are:

    * The Advanced button is replaced by the Show Less/More link
    * The Show less/more link controls advanced elements only within the section
      it is located at
    * The Show less/more state of sections is preserved between form submissions
    * When javascript is off, all advanced elements will be displayed by default,
      no show/hide controls will exists on the page