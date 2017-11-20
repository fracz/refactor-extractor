commit e721d0126f30c1230068908c473f050e3f7fe802
Author: Sachin Sudheendra <sachin.sudheendra@gmail.com>
Date:   Wed Jul 30 11:46:58 2014 +0530

    Delegating OauthRepository methods calls to duplicate methods to maintain compatibility between Rails2 plugin and Rails4 Engine.

    This is necessary because refactoring the method names will break the Rails2 plugin calls. Because we have 2 operating modes right now, I've no other option but to maintain duplicate/delegating methods.