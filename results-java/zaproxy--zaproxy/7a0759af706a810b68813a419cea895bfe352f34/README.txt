commit 7a0759af706a810b68813a419cea895bfe352f34
Author: thc202 <thc202@gmail.com>
Date:   Wed Mar 23 23:45:34 2016 +0000

    Use Context factories when installed

    Use existing custom context data when adding a ContextDataFactory and
    add custom context panels when adding ContextFactoryPanel, after full
    initialisation (for example, when an add-on is installed), so that
    existing Contexts show the correct data and custom panels.

    Change class Control to notify the class Model once the initialisation
    is done, by calling the (new) instance method Model.postInit().
    Change class Model to call the method loadContextData(Session, Context)
    on the added ContextDataFactory for all existing contexts, if it's being
    added after the initialisation so that any existing custom context data
    that's in the session is used/loaded, otherwise the data would be
    missing when re-installing/updating an add-on.
    Change class View to add the panels to all existing contexts after
    initialisation, when a ContextPanelFactory is added to the view. Also,
    some other refactorings were made:
     - Extract a method that obtains the ContextGeneralPanel of a context,
     used to create the path of the panels added;
     - Extract a method that adds the panels from a ContextPanelFactory for
     a context;
     - Reuse the same panel path (and contexts node name) when adding panels
     to an added Context, in method addContext(Context).

    Fix #2331 - Custom Context Panels not show in existing contexts after
    installation of add-on