commit e83157d2e98e44c21f2cf8931421d5a3d47fd852
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Sun Oct 10 12:59:01 2010 -0400

    [al milestone] Refactor helper/filter usage in Zend\View

    - Added HelperLoader and HelperBroker, extending PluginClassLoader and
      PluginBroker, respectively
      - Refactored Zend\View to use HelperBroker for resolving/registering helpers
    - Refactored Zend\View to use FilterChain for filter chain
    - Added "Variables" class:
      - Registry for variables
      - Provides escape mechanism, including encoding
        - Auto-escapes variables
        - Provides access to raw values
      - Provides "strict" access (trigger notice on undefined variables)
    - Refactored Zend\View to use Variables object by default to register variables
      - todo: allow creating a "default" variable container when render() is passed
        variables
    - Added TemplateResolver interface, with method getScriptName()
    - Added TemplatePathStack:
      - Uses Zend\Stdlib\SplStack internally
      - Implements view stream for short tags
      - Implements LFI protections
    - Refactored Zend\View to use TemplatePathStack by default
    - Renamed ViewEngine interface to Renderer
      - Removed all methods except render() and getEngine()
    - Removed View class
    - Renamed AbstractView to PhpRenderer
      - no longer abstract
    - TODO
      - allow passing classes and options for each helping class (resolver,
        variables, broker, filter chain)
      - move engine implementation into a separate directory?
        - would include PhpRenderer, potentially TemplateResolver classes
      - refactor existing unit tests and write new ones