commit 1ed403d424fa643a4c9a245b6cc56359782295bf
Author: Brian Teeman <brian@teeman.net>
Date:   Mon May 22 19:34:48 2017 +0100

    [a11y] com_contenthistory on mobile devices (#15828)

    * [a11y] com_contenthistory on mobile devices

    Recent changes have aimed to improve the accessibility where we use font icons to ensure that there value is not read by a screenreader and only the associated text is read.

    The buttons in the version history modal are not constructed the same as other buttons in Joomla and when we switch to a mobile device the displayed text is hidden.

    This means that with the current markup it is all ok for accessibility on a desktop because we have the associated text but as this text is hidden on a phone the assistive technology has no way of identifing the buttons.

    This PR adds the aria-label attribute to the buttons to ensure that assistive technology can always "read" the button at all screensizes.

    Thanks to @fuzzbomb for spotting this and his guidance on resolving this

    * typo