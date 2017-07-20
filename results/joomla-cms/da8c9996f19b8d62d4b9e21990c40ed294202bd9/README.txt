commit da8c9996f19b8d62d4b9e21990c40ed294202bd9
Author: Hannes Papenberg <info@joomlager.de>
Date:   Sun Jun 14 15:20:59 2009 +0000

    Implemented nested categories:
    - removed com_sections
    - refactored com_categories to MVC
    - added JTableNested to support Nested Sets
    - changed com_content, com_contacts, com_weblinks, com_newsfeeds to use the new system. com_contacts and com_weblinks are not finally done
    - changed content modules to use the nested categories
    - changed JForm, JHTML and JParameter elements to use the new nested categories

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@12070 6f6e1ebd-4c2b-0410-823f-f34bde69bce9