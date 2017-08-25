commit d72ff7a208673f6cc514e6a9d5028c0263bedb66
Author: dongsheng <dongsheng>
Date:   Wed Jul 2 09:24:29 2008 +0000

    MDL-15488.
    1. Remove all the pear stuff in flickr plug-in.
    2. Remove database cache for flickr plugin-in, it will be back soon.
    3. Remove uploading, repository doesn't need it.
    4. Create a curl wrapper class by myself, so no license issues anymore, I will continue to improve it.