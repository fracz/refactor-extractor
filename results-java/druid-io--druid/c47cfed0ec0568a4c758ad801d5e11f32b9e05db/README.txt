commit c47cfed0ec0568a4c758ad801d5e11f32b9e05db
Author: kaijianding <kaijian.ding@gmail.com>
Date:   Thu Apr 27 20:11:07 2017 +0800

    Significantly improve LongEncodingStrategy.AUTO build performance (#4215)

    * Significantly improve LongEncodingStrategy.AUTO build performance

    * use numInserted instead of tempIn.available

    * fix bug