commit 47a8282b396e3b4a5412c06f3873e6ff8d2ebbfc
Author: Ian Lake <ilake@google.com>
Date:   Tue Sep 6 14:37:14 2016 -0700

    Improve the workflow for importing photos with GET_CONTENT

    The previous workflow for importing photos had a number of downsides:
    - The warning dialog felt punitive towards the user even though it wasn't their choice that the app they want to use only supports GET_CONTENT
    - The standard Storage Access Framework UI allowed users to import photos that they should really be adding via ACTION_OPEN_DOCUMENT

    Since we can query the exact activities supporting GET_CONTENT, we can improve the workflow by:
    - Handling only one app by directly starting that Activity
    - Handling more than one app by having the user choose which app they want to import photos from

    This has the side benefit that there is no need for a scary dialog anymore and photos cannot be imported if adding them directly is an option