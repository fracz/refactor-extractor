commit 681db52ba62e8169b5534c8dccda4618e1140b2e
Author: Carsten Brandt <mail@cebe.cc>
Date:   Mon Aug 3 23:02:56 2015 +0200

    refactored SchemaBuilder

    - rename class to ColumnSchemaBuilder as this is more appropriate
    - changed internal organisation to match how the rest of schema related classes work
      - the ColumnSchemaBuilder is now created the same way as QueryBuilder is
    - removed static call magic and method annotations, now real methods are called as they are
    - the whole code works on objects in a db context now instead of setting database connection in global state
    - trait is now used by Migration by default but can be used in other contexts as well

    Migration usage is now as follows:

    ```php
    $this->createTable('example_table', [
      'id' => $this->primaryKey(),
      'name' => $this->string(64)->notNull(),
      'type' => $this->integer()->notNull()->defaultValue(10),
      'description' => $this->text(),
      'rule_name' => $this->string(64),
      'data' => $this->text(),
      'created_at' => $this->datetime()->notNull(),
      'updated_at' => $this->datetime(),
    ]);
    ```