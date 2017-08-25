commit 26f8822d5c40a1cd4049fc09cb6da46e6cd55f7d
Author: Damyon Wiese <damyon@moodle.com>
Date:   Tue Feb 4 12:51:21 2014 +0800

    MDL-43867 Atto: Accessibility improvements.

    1/ Set the aria-labelledby attribute on the contenteditable div (find the label from
    original textarea)

    2/ Store/restore the selection for the contenteditable div when it is focused. This allows
    you to select some text, then go to the toolbar and click a button, and the selection
    will be restored before the button effect is applied.

    3/ Add an accessibility helper plugin.

    From testing in all screenreaders, I found that all of their support for contenteditable is not great.
    They treat it like a textbox - which means you can type and edit text, but it tells you nothing about
    the styles, links or images in the editor. So I added a button to the toolbar, that is only accessible
    when navigating via keyboard, that opens an accesssibility helper dialogue. The dialogue shows the list
    of current styles, a global list of all links, and a global list of all images. Choosing an image or link
    from here, will focus on the editable region, and select the link/image.

    4/ Add an accessibility checker plugin to Atto.

    Checks for images with no alt, images and links with filenames as alternate text/link text, and contrast ratios
    less than WCAG 2.0 AA.