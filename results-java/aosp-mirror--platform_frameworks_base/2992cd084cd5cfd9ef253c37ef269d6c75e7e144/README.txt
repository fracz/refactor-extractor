commit 2992cd084cd5cfd9ef253c37ef269d6c75e7e144
Author: Jack He <siyuanh@google.com>
Date:   Tue Aug 22 21:21:23 2017 -0700

    Fix checkstyle errors (2/2)

    * Manual style corrections with IDE assistance
    * Variable name refactors are done through IDE
    * Corrected general style errors such as:
      - "final private var" -> "private final var"
      - "&&", "+", "||" should not be at the end of line
      - Non-static private variable should be like "mVar"
      - Private static variable should be like "sVar"
      - Code file should always end with newline
      - Inherited methods should be annotated with @Override
        and no @hide tags
      - Public methods should always have a JavaDoc entry
      - "int[] array" is preferred over "int array[]"
      - private methods should be accessed without "this."
        when there is no name collisions.
      - "boolean ? true : false" -> boolean
      - "boolean ? false : true" -> !boolean
      - "boolean == true" OR "boolean != false" -> boolean
      - "boolean != true" OR "boolean == false" -> !boolean

    Bug: 63596319
    Test: make checkbuild, no functional changes
    Change-Id: Iabdc2be912a32dd63a53213d175cf1bfef268ccd