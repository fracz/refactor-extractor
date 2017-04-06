commit bf1dfbbe99d610a923db9b0444bec7005d69b0e9
Author: Bernhard Schussek <bschussek@gmail.com>
Date:   Wed May 4 16:58:39 2011 +0200

    [Form] Added test for last commit by kriswallsmith and improved dealing with original names

    The form component should now guarantee to always pass an UploadedFile object to your model. There you can call getOriginalName() to retrieve the original name of the uploaded file. For security reasons, the real file name is a generated hash value.