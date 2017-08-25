commit 23c65c383945cfc9c2293f45a7cbc6f1a68178ec
Author: terrafrost <terrafrost@php.net>
Date:   Thu Mar 19 07:53:19 2015 -0500

    SSH2: timeout improvements

    make it so that the timeout in the constructor behaves in the same
    way that timeout's set via setTimeout() do. eg. isTimeout() tells
    you if a timeout was thrown etc.