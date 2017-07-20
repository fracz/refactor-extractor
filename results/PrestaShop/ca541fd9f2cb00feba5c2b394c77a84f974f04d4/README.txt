commit ca541fd9f2cb00feba5c2b394c77a84f974f04d4
Author: Adonis Karavokyros <prestarocket@users.noreply.github.com>
Date:   Fri Sep 11 16:39:23 2015 +0200

    [*][–] FO : improve smarty hook fct : add exclude param

    If you call a module with {hook h="displayNav" mod="blockcontact"} and blockcontact module is uninstalled or doesn't exist anymore, all modules enabled in the hook displayNav are displayed;
    You can test it with this code in your tpl :
    {hook h="displayNav" mod="modulenoexist"}

    I also add a parameter to exclude some module form the hook calling