commit a242a63817d031debec4090c58f8fcaaccfd7281
Author: Adrien Grand <jpountz@gmail.com>
Date:   Thu Aug 7 11:15:46 2014 +0200

    [DOCS] Remove the section about codecs.

    This documentation was dangerous because it felt like it was possible to gain
    substantial performance by just switching the codec of the index.

    However, non-default codecs are dangerous to use since they are not supported
    in terms of backward compatibility, and most improvements that they bring have
    been folded into the default codec anyway (for example, the default codec
    "pulses" postings lists that contain a single document).