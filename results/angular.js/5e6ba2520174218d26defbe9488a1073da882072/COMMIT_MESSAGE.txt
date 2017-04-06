commit 5e6ba2520174218d26defbe9488a1073da882072
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Mar 12 15:54:48 2012 -0700

    fix(forms): remove the need for extra form scope

    the forms/controls code refactored not to depend on events which forced
    us to create new scope for each form element.