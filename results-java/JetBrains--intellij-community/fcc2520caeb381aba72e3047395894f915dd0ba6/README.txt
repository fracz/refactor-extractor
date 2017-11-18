commit fcc2520caeb381aba72e3047395894f915dd0ba6
Author: Bas Leijdekkers <basleijdekkers@gmail.com>
Date:   Fri Feb 18 20:47:34 2011 +0100

    Replace if with switch intention improvements:
    prevent unnecessary braces around switch branches
    if braces are inserted place the break statement inside the braces
    smaller cleaner code