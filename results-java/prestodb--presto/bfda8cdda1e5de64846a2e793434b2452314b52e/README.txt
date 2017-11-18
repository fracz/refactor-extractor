commit bfda8cdda1e5de64846a2e793434b2452314b52e
Author: Christopher Berner <christopherberner@gmail.com>
Date:   Fri Nov 6 11:23:18 2015 -0800

    Optimize greater_than operator for arrays

    Optimize greater_than operator for arrays with elements that use long as
    their native container type. This improves efficency by 2-3x