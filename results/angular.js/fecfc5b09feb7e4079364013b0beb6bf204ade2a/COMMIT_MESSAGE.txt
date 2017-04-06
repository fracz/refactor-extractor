commit fecfc5b09feb7e4079364013b0beb6bf204ade2a
Author: Igor Minar <igor@angularjs.org>
Date:   Fri Aug 1 15:48:41 2014 -0700

    perf($parse): speed up fn invocation by optimizing arg collection

    8-15% improvement for depending on the number of args