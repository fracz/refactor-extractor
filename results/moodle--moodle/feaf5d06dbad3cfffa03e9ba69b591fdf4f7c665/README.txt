commit feaf5d06dbad3cfffa03e9ba69b591fdf4f7c665
Author: skodak <skodak>
Date:   Thu Dec 28 21:21:44 2006 +0000

    MDL-8015 improved file uploading
    - changed file upload api in formslib
    - fixed blog attachments and related code in file.php
    - fixed glossary attachments
    - fixed embedded images in forum posts and blogs - only gif, png and jpeg; the problme was that svg were embedded using img tag which was wrong, the same applied to other picture formats unsupported by browsers (please note that student submitted svg should be never embedded in moodle page for security reasons)
    - other minor fixes