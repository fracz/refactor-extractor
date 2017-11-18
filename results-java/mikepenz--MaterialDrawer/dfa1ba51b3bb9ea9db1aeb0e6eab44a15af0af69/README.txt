commit dfa1ba51b3bb9ea9db1aeb0e6eab44a15af0af69
Author: Mike Penz <mikepenz@gmail.com>
Date:   Thu Mar 26 22:21:41 2015 +0100

    * improve onClick and onProfileChanged listener interfaces to allow returning true if you have consumed the event and don't want further action.
    * add boolean to onProfileChanged with info if the current clicked profile was the current profile