commit 26d77ecd2e06baf321f3381730db81403d7b205a
Author: Eric Anderson <ejona@google.com>
Date:   Thu Jul 23 10:09:30 2015 -0700

    Minor readability changes

    Improved some consistency. writeHeaders was the only non-final
    implementation method of ServerStream, even though it is really no
    different than the others.