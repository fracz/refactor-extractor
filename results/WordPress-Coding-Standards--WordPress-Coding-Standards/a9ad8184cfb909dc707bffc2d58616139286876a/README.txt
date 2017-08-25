commit a9ad8184cfb909dc707bffc2d58616139286876a
Author: JDGrimes <jdg@codesymphony.co>
Date:   Sun Mar 15 17:49:31 2015 -0400

    Add init() method to be called by child classes

    See
    https://github.com/WordPress-Coding-Standards/WordPress-Coding-Standards
    /issues/246#issuecomment-80720786

    Also includes improvements for the whitelisting comment checking
    method, which now handles cases where PHP and HTML are interspersed.