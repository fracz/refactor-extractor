<?php

    /**
     *
     * Idiorm
     *
     * A single-class super-simple database abstraction layer for PHP.
     * Provides (nearly) zero-configuration object-relational mapping
     * and a fluent interface for building basic, commonly-used queries.
     *
     * Version 0.1
     *
     * BSD Licensed.
     *
     * Copyright (c) 2009, Jamie Matthews
     * All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are met:
     *
     * * Redistributions of source code must retain the above copyright notice, this
     *   list of conditions and the following disclaimer.
     *
     * * Redistributions in binary form must reproduce the above copyright notice,
     *   this list of conditions and the following disclaimer in the documentation
     *   and/or other materials provided with the distribution.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
     * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
     * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
     * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
     * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
     * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
     * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
     * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
     * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     *
     */

    class ORM {

        // ----------------------- //
        // --- CLASS CONSTANTS --- //
        // ----------------------- //

        // Select WHERE operators
        // These are "public" API and should
        // be used (if needed) as the third
        // argument to the where() method.
        const EQUALS = '=';
        const LIKE = 'LIKE';

        // The rest are "private" API.

        // Find types
        const FIND_ONE = 0;
        const FIND_MANY = 1;

        // Update or insert?
        const UPDATE = 0;
        const INSERT = 1;

        // Where clauses array keys
        const COLUMN_NAME = 0;
        const VALUE = 1;
        const OPERATOR = 2;

        // ------------------------ //
        // --- CLASS PROPERTIES --- //
        // ------------------------ //

        // Class configuration
        private static $config = array(
            'connection_string' => 'sqlite://:memory:',
            'id_column' => 'id',
            'id_column_overrides' => array(),
        );

        // Database connection, instance of the PDO class
        private static $db;

        // --------------------------- //
        // --- INSTANCE PROPERTIES --- //
        // --------------------------- //

        // The name of the table the current ORM instance is associated with
        private $table_name;

        // Will be FIND_ONE or FIND_MANY
        private $find_type;

        // Values to be bound to the query
        private $values = array();

        // Array of WHERE clauses
        private $where = array();

        // The data for a hydrated instance of the class
        private $data = array();

        // Fields that have been modified during the
        // lifetime of the object
        private $dirty_fields = array();

        // Are we updating or inserting?
        private $update_or_insert = self::UPDATE;

        // ---------------------- //
        // --- STATIC METHODS --- //
        // ---------------------- //

        /**
         * Pass configuration settings to the class in the form of
         * key/value pairs. As a shortcut, if the second argument
         * is omitted, the setting is assumed to be the DSN string
         * used by PDO to connect to the database. Often, this
         * will be the only configuration required to use Idiorm.
         */
        public static function configure($key, $value=null) {
            // Shortcut: If only one argument is passed,
            // assume it's a connection string
            if (is_null($value)) {
                $value = $key;
                $key = 'connection_string';
            }
            self::$config[$key] = $value;
        }

        /**
         * Despite its slightly odd name, this is actually the factory
         * method used to acquire instances of the class. It is named
         * this way for the sake of a readable interface, ie
         * ORM::for_table('table_name')->find_one()-> etc. As such,
         * this will normally be the first method called in a chain.
         */
        public static function for_table($table_name) {
            return new self($table_name);
        }

        /**
         * Set up the database connection used by the class.
         */
        private static function setup_db() {
            self::$db = new PDO(self::$config['connection_string']);
            self::$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        }

        /**
         * This can be called if the ORM should use a ready-instantiated
         * PDO object as its database connection. Won't be used in normal
         * operation, but it's here in case it's needed.
         */
        public static function set_db($db) {
            self::$db = $db;
        }

        /**
         * Returns the PDO instance used by the the ORM to communicate with
         * the database. This can be called if any low-level DB access is
         * required outside the class.
         */
        public static function get_db() {
            if (!is_object(self::$db)) {
                self::setup_db();
            }
            return self::$db;
        }

        // ------------------------ //
        // --- INSTANCE METHODS --- //
        // ------------------------ //

        /**
         * Private constructor; can't be called directly.
         * Use a factory method instead (probably ORM::for_table)
         */
        private function __construct($table_name, $data=array()) {
            $this->table_name = $table_name;
            $this->data = $data;
        }

        /**
         * Create a new, empty instance of the class. Used
         * to add a new row to your database. May optionally
         * be passed an associative array of data to populate
         * the instance. If so, all fields will be flagged as
         * dirty so all will be saved to the database when
         * save() is called.
         */
        public function create($data=null) {
            $this->update_or_insert = self::INSERT;

            if (!is_null($data)) {
                return $this->hydrate($data)->force_all_dirty();
            }
            return $this;
        }

        /**
         * Tell the ORM that you are expecting a single result
         * back from your query. If this method has been called
         * in your chain, when you call run() you will receive
         * a single instance of the ORM class, or false if no
         * rows were returned.
         */
        public function find_one() {
            $this->find_type = self::FIND_ONE;
            return $this;
        }

        /**
         * Tell the ORM that you are expecting multiple results
         * from your query. If this method has been called in your
         * chain, when you call run() you will receive an array
         * of instances of the ORM class, or an empty array if
         * no rows were returned.
         */
        public function find_many() {
            $this->find_type = self::FIND_MANY;
            return $this;
        }

         /**
         * This method can be called hydrate (populate) this
         * instance of the class from an associative array of data.
         * This will usually be called only from inside the class,
         * but it's public in case you need to call it directly.
         */
        public function hydrate($data=array()) {
            $this->data = $data;
            return $this;
        }

        /**
         * Force the ORM to flag all the fields in the $data array
         * as "dirty" and therefore update them when save() is called.
         */
        public function force_all_dirty() {
            $this->dirty_fields = $this->data;
            return $this;
        }

        /**
         * Add a WHERE clause to your query. Each time this is called
         * in the chain, an additional WHERE will be added, and these
         * will be ANDed together when the final query is built.
         * By default, the operator used is '=', but the third
         * parameter to this method may be used to indicate other
         * operators such as LIKE. Class constants should be used to
         * provide this operator.
         */
        public function where($column_name, $value, $operator=self::EQUALS) {
            $this->where[] = array(
                self::COLUMN_NAME => $column_name,
                self::VALUE => $value,
                self::OPERATOR => $operator
            );
            return $this;
        }

        /**
         * Build a SELECT statement based on the clauses that have
         * been passed to this instance by chaining method calls.
         */
        private function build_select() {
            $query = array();
            $query[] = 'SELECT * FROM ' . $this->table_name;

            if (count($this->where) > 0) {
                $query[] = "WHERE";
                $first = array_shift($this->where);
                $query[] = join(" ", array(
                    $first[self::COLUMN_NAME],
                    $first[self::OPERATOR],
                    '?'
                ));
                $this->values[] = $first[self::VALUE];

                while($where = array_shift($this->where)) {
                    $query[] = "AND";
                    $query[] = join(" ", array(
                        $where[self::COLUMN_NAME],
                        $where[self::OPERATOR],
                        '?'
                    ));
                    $this->values[] = $where[self::VALUE];
                }
            }

            return join(" ", $query);
        }

        /**
         * Execute the SELECT query that has been built up by chaining methods
         * on this class. This should usually be the last method in your chain.
         * If find_one() has been called, this will return a single instance of
         * the class or false. If find_many() has been called, this will return
         * an array of instances of the class.
         */
        public function run() {
            self::setup_db();
            $statement = self::$db->prepare($this->build_select());
            $statement->execute($this->values);

            if ($this->find_type == self::FIND_ONE) {
                $result = $statement->fetch(PDO::FETCH_ASSOC);
                return $result ? self::for_table($this->table_name)->hydrate($result) : $result;
            } else {
                $instances = array();
                while ($row = $statement->fetch(PDO::FETCH_ASSOC)) {
                    $instances[] = self::for_table($this->table_name)->hydrate($row);
                }
                return $instances;
            }
        }

        /**
         * For debugging only. Returns a string representation of the query
         * that would be executed by calling run() on the current instance of the
         * class. Because PDO works using prepared statements, this can provide
         * only a rough representation of the query, but this will usually be enough
         * to check that your query has been build as expected.
         */
        public function as_sql() {
            $sql = $this->build_select();
            $sql = str_replace("?", "%s", $sql);

            $quoted_values = array();
            foreach ($this->values as $value) {
                $quoted_values[] = '"' . $value . '"';
            }
            return vsprintf($sql, $quoted_values);
        }

        /**
         * Return the value of a property of this object (database row)
         * or null if not present.
         */
        public function get($key) {
            return isset($this->data[$key]) ? $this->data[$key] : null;
        }

        /**
         * Return the name of the column in the database table which contains
         * the primary key ID of the row.
         */
        private function get_id_column_name() {
            if (isset(self::$config['id_column_overrides'][$this->table_name])) {
                return self::$config['id_column_overrides'][$this->table_name];
            } else {
                return self::$config['id_column'];
            }
        }

        /**
         * Get the primary key ID of this object.
         */
        public function id() {
            return $this->get($this->get_id_column_name());
        }

        /**
         * Set a property to a particular value on this object.
         * Flags that property as 'dirty' so it will be saved to the
         * database when save() is called.
         */
        public function set($key, $value) {
            $this->data[$key] = $value;
            $this->dirty_fields[$key] = $value;
        }

        /**
         * Save any fields which have been modified on this object
         * to the database.
         */
        public function save() {
            $query = array();
            $values = array_values($this->dirty_fields);

            if ($this->update_or_insert == self::UPDATE) {

                // If there are no dirty values, do nothing
                if (count($values) == 0) {
                    return true;
                }

                $query[] = "UPDATE";
                $query[] = $this->table_name;
                $query[] = "SET";

                $field_list = array();
                foreach ($this->dirty_fields as $key => $value) {
                    $field_list[] = "$key = ?";
                }
                $query[] = join(", ", $field_list);

                $query[] = "WHERE";
                $query[] = $this->get_id_column_name();
                $query[] = "= ?";
                $values[] = $this->id();

            } else {

                $query[] = "INSERT INTO";
                $query[] = $this->table_name;
                $query[] = "(" . join(", ", array_keys($this->dirty_fields)) . ")";
                $query[] = "VALUES";

                $placeholders = array();
                for ($i=0; $i<count($this->dirty_fields); $i++) {
                    $placeholders[] = "?";
                }

                $query[] = "(" . join(", ", $placeholders) . ")";
            }

            $query = join(" ", $query);
            $statement = self::$db->prepare($query);
            $statement->execute($values);
            return true;
        }
    }
