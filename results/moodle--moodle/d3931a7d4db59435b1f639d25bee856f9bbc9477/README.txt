commit d3931a7d4db59435b1f639d25bee856f9bbc9477
Author: Damyon Wiese <damyon@moodle.com>
Date:   Fri Apr 11 14:15:02 2014 +0800

    MDL-45034 Atto: Image dialogue improvements.

    1. Fix Nan bugs in auto width / height
    2. Allow percentages in auto width / height
    3. Change dialogue title
    4. Prevent preview image resizing from changing the height of the dialogue.
    5. Change wording to "Auto size"
    6. Auto adjust size when the "Auto size" checkbox is toggled.
    7. Add img-responsive to images with the original aspect ratio.
    8. If the width and height fields are left blank, revert them to the image size.