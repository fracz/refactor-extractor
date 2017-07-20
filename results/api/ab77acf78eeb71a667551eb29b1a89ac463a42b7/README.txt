commit ab77acf78eeb71a667551eb29b1a89ac463a42b7
Author: Jason Lewis <jason.lewis1991@gmail.com>
Date:   Mon Mar 21 23:15:44 2016 +1030

    Lots of refactoring and tidying.

    Main changes here are to the service providers. Move a lot of logic into multiple
    providers to keep the files cleaner. All providers extend from an abstract
    provider that has a config method to grab and instantiate config values.

    Also includes a fix to set the controller element in the action when the
    uses element is a controller, this allows the getActionName method to
    return the proper controller@method name in the api:routes command.