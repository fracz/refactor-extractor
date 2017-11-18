commit 171c592f0b7066acf279863c8a52ddabea49d3db
Author: Romain Guy <romainguy@google.com>
Date:   Thu Jan 6 10:04:23 2011 -0800

    New layers API for Views.

    This API can be used to back a view and its children with either a
    software layer (bitmap) or hardware layer (FBO). Layers have
    various usages, including color filtering and performance
    improvements during animations.

    Change-Id: Ifc3bea847918042730fc5a8c2d4206dd6c9420a3