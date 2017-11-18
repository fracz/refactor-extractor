commit 787d8fc0215c5bab541f61c2d69a91753d462559
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Tue Jan 22 13:28:16 2013 -0800

    Refactoring towards performance improvements

    - Convert operators to implement Func1 instead of extend Observable
    - Change Observable to concete instead of abstract
    - subscribe is now controlled by the concrete class so we can control the wrapping of a Func to be executed when subscribe is called
    - removed most wrapping inside operators with AtomicObservable except for complex operators like Zip that appear to still need it

    While doing these changes the util/function packages got moved a little as well to make more sense for where the Atomic* classes should go