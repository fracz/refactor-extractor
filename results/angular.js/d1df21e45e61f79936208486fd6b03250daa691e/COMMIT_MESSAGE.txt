commit d1df21e45e61f79936208486fd6b03250daa691e
Author: Shahar Talmi <shahar.talmi@gmail.com>
Date:   Wed Jul 23 01:15:59 2014 +0300

    refactor(Angular): add isPromiseLike helper function

    This can be used internally to remove the repeating pattern of `obj && obj.then`. For now, I don't see a good reason to expose this in angular's public interface.