commit 60d98814e1096c2b43fd03f10aa41fddb525e3c1
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Thu May 3 16:03:28 2012 -0500

    Initial refactor of Zend\Captcha

    - Renamed interfaces
    - Modified interface: s/getDecorator/getHelperName/, removed render()
    - Working Captcha\Dumb tests
    - Need to complete other captcha tests