commit afdbe19937a7fe30fae2a03256616ecca9941b23
Author: Eric Anderson <ejona@google.com>
Date:   Thu Jul 23 10:50:02 2015 -0700

    Minor fixes/improvements

    Cancel was the only method for implementing ClientStream that was not
    final, even though it isn't really any different from the other methods.