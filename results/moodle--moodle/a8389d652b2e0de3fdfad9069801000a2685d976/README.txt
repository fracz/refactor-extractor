commit a8389d652b2e0de3fdfad9069801000a2685d976
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon Jan 4 17:43:02 2010 +0000

    Edit grading form refactoring

    The previous desing was too "base-class oriented". I have realized that
    strategies have to deal with loading and saving dimension definitions in
    their own way. Getting rid of all db<->form field mappings as it would
    work for very simple strategies only.