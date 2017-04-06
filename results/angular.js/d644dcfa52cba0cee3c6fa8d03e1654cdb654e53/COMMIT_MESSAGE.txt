commit d644dcfa52cba0cee3c6fa8d03e1654cdb654e53
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Nov 26 20:25:38 2012 +0100

    fix(ngRepeat): attempt to simplify and improve existing fix for #933

    I'm keeping this in for future reference. The issue with this solution
    is that if we shift() the first item in the array, the whole repeater
    DOM will be rebuilt from scratch, we need to do better than that.