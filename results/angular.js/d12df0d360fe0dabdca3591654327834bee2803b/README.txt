commit d12df0d360fe0dabdca3591654327834bee2803b
Author: Misko Hevery <misko@hevery.com>
Date:   Tue Oct 25 22:21:21 2011 -0700

    refactor(compiler) turn compiler into a service

    BREAK
    - remove angular.compile() since the compile method is now a service and needs to be injected