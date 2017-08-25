commit a926cb727b405718f8ab534ecaaf97c22d60c86f
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Fri May 31 10:40:06 2013 -0400

    Refactor search into new Endpoint architecture

    The functionality of search has been refactored into an
    Endpoint class.  This will allow considerable code reuse
    between various endpoints, as well as making generation of
    the endpoint classes much easier.