<?php
/**
 * RedBean MySQLWriter
 * @package 		RedBean/QueryWriter/MySQL.php
 * @description		Writes Queries for MySQL Databases
 * @author			Gabor de Mooij
 * @license			BSD
 */
class RedBean_QueryWriter_MySQL implements RedBean_QueryWriter {
	/**
         *
	 * @var array all allowed sql types
	 */




	public $typeno_sqltype = array(
		" TINYINT(3) UNSIGNED ",
		" INT(11) UNSIGNED ",
		" BIGINT(20) ",
		" VARCHAR(255) ",
		" TEXT ",
		" LONGTEXT "
		);

		/**
		 *
		 * @var array all allowed sql types
		 */
		public $sqltype_typeno = array(
		"tinyint(3) unsigned"=>0,
		"int(11) unsigned"=>1,
		"bigint(20)"=>2,
		"varchar(255)"=>3,
		"text"=>4,
		"longtext"=>5
		);

		/**
		 * @var array all dtype types
		 */
		public $dtypes = array(
		"tintyintus","intus","ints","varchar255","text","ltext"
		);


                private $adapter;

                public function __construct( RedBean_DBAdapter $adapter ) {
                    $this->adapter = $adapter;


                    $this->adapter->exec("
				CREATE TABLE IF NOT EXISTS `dtyp` (
				  `id` int(11) unsigned NOT NULL auto_increment,
				  `tinyintus` tinyint(3) unsigned NOT NULL,
				  `intus` int(11) unsigned NOT NULL,
				  `ints` bigint(20) NOT NULL,
				  `varchar255` varchar(255) NOT NULL,
				  `text` text NOT NULL,
				  PRIMARY KEY  (`id`)
				) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
				");




                }




		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryWiden( $options ) {
			extract($options);
			return "ALTER TABLE `$table` CHANGE `$column` `$column` $newtype ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryAddColumn( $options ) {
			extract($options);
			return "ALTER TABLE `$table` ADD `$column` $type ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUpdate( $options ) {
			extract($options);
			$update = array();
			foreach($updatevalues as $u) {
				$update[] = " `".$u["property"]."` = \"".$u["value"]."\" ";
			}
			return "UPDATE `$table` SET ".implode(",",$update)." WHERE id = ".$id;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryInsert( $options ) {

			extract($options);

			foreach($insertcolumns as $k=>$v) {
				$insertcolumns[$k] = "`".$v."`";
			}

			foreach($insertvalues as $k=>$v) {
				$insertvalues[$k] = "\"".$v."\"";
			}

			$insertSQL = "INSERT INTO `$table`
					  ( id, ".implode(",",$insertcolumns)." )
					  VALUES( null, ".implode(",",$insertvalues)." ) ";
			return $insertSQL;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryCreate( $options ) {
			extract($options);
			return "INSERT INTO `$table` (id) VALUES(null) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryInferType( $options ) {
			extract($options);
			$v = "\"".$value."\"";
			$checktypeSQL = "insert into dtyp VALUES(null,$v,$v,$v,$v,$v )";
			return $checktypeSQL;
		}

		/**
		 *
		 * @return string $query
		 */
		private function getQueryResetDTYP() {
			return "truncate table dtyp";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryRegisterTable( $options ) {
			extract( $options );
			return "replace into redbeantables values (null, \"$table\") ";
		}
		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUnregisterTable( $options ) {
			extract( $options );
			return "delete from redbeantables where tablename = \"$table\" ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryRelease( $options ) {
			extract( $options );
			return "DELETE FROM locking WHERE fingerprint=\"".$key."\" ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryRemoveExpirLock( $options ) {
			extract( $options );
			return "DELETE FROM locking WHERE expire <= ".(time()-$locktime);
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUpdateExpirLock( $options ) {
			extract( $options );
			return "UPDATE locking SET expire=".$time." WHERE id =".$id;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryAQLock( $options ) {
			extract($options);
			return "INSERT INTO locking VALUES(\"$table\",$id,\"".$key."\",\"".$time."\") ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryDistinct($options) {
			extract($options);
			return "SELECT id FROM `$type` GROUP BY $field";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryFastLoad( $options ) {
			extract( $options );
			return "SELECT * FROM `$type` WHERE id IN ( ".implode(",", $ids)." ) ORDER BY FIELD(id,".implode(",", $ids).") ASC		";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryWhere($options) {
			extract($options);
			return "select `$table`.id from $table where ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryFind($options) {

			extract($options);
			$db = RedBean_OODB::getInstance()->getToolBox()->getDatabase();
			$findSQL = "SELECT id FROM `$tbl` WHERE ";


			foreach($bean as $p=>$v) {
				if ($p === "type" || $p === "id") continue;
				$p = $db->escape($p);
				$v = $db->escape($v);
				if (isset($searchoperators[$p])) {
					if ($searchoperators[$p]==="LIKE") {
						$part[] = " `$p`LIKE \"%$v%\" ";
					}
					else {
						$part[] = " `$p` ".$searchoperators[$p]." \"$v\" ";
					}
				}
				else {
				}
			}
			if ($extraSQL) {
				$findSQL .= @implode(" AND ",$part) . $extraSQL;
			}
			else {
				$findSQL .= @implode(" AND ",$part) . " ORDER BY $orderby LIMIT $start, $end ";
			}
			return $findSQL;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryList($options) {
			extract($options);
			$db = RedBean_OODB::getInstance()->getToolBox()->getDatabase();
			if ($extraSQL) {
				$listSQL = "SELECT * FROM ".$db->escape($type)." ".$extraSQL;
			}
			else {
				$listSQL = "SELECT * FROM ".$db->escape($type)."
			ORDER BY ".$orderby;
				if ($end !== false && $start===false) {
					$listSQL .= " LIMIT ".intval($end);
				}
				if ($start !== false && $end !== false) {
					$listSQL .= " LIMIT ".intval($start).", ".intval($end);
				}
				if ($start !== false && $end===false) {
					$listSQL .= " LIMIT ".intval($start).", 18446744073709551615 ";
				}
			}
			return $listSQL;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryAddAssocNow( $options ) {
			extract($options);
			return "REPLACE INTO `$assoctable` VALUES(null,$id1,$id2) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUnassoc( $options ) {
			extract($options);
			return "DELETE FROM `$assoctable` WHERE ".$t1."_id = $id1 AND ".$t2."_id = $id2 ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryCreateAssoc($options) {

			extract($options);

			return "
			 CREATE TABLE `$assoctable` (
			`id` INT( 11 ) UNSIGNED NOT NULL AUTO_INCREMENT ,
			`".$t1."_id` INT( 11 ) UNSIGNED NOT NULL,
			`".$t2."_id` INT( 11 ) UNSIGNED NOT NULL,
			 PRIMARY KEY ( `id` )
			 ) ENGINE = ".$engine.";
			";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUntree( $options ) {
			extract($options);
			return "DELETE FROM `$assoctable2` WHERE
				(parent_id = $idx1 AND child_id = $idx2) OR
				(parent_id = $idx2 AND child_id = $idx1) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryAddAssoc($options) {
			extract( $options );
			return "ALTER TABLE `$assoctable` ADD UNIQUE INDEX `u_$assoctable` (`".$t1."_id`, `".$t2."_id` ) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryDeltreeType($options) {
			extract( $options );
			return "DELETE FROM $assoctable WHERE parent_id = $id  OR child_id = $id ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryCreateTree( $options ) {
			extract( $options );
			return "
				 CREATE TABLE `$assoctable` (
				`id` INT( 11 ) UNSIGNED NOT NULL AUTO_INCREMENT ,
				`parent_id` INT( 11 ) UNSIGNED NOT NULL,
				`child_id` INT( 11 ) UNSIGNED NOT NULL,
				 PRIMARY KEY ( `id` )
				 ) ENGINE = ".$engine.";
				";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUnique( $options ) {
			extract( $options );
			return "ALTER TABLE `$assoctable` ADD UNIQUE INDEX `u_$assoctable` (`parent_id`, `child_id` ) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryAddChild( $options ) {
			extract( $options );
			return "REPLACE INTO `$assoctable` VALUES(null,$pid,$cid) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryRemoveChild( $options ) {
			extract( $options );
			return "DELETE FROM `$assoctable` WHERE
				( parent_id = $pid AND child_id = $cid ) ";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryDescribe( $options ) {
			extract( $options );
			return "describe `$table`";
		}


		/**
	     * Returns an array of table Columns
	     * @return array
	     */
	    public function getTableColumns($tbl, RedBean_DBAdapter $db) {
	        $rs = $db->get($this->getQuery("describe",array(
	            "table"=>$tbl
	        )));

	        return $rs;
	    }


		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryDropTables( $options ) {
			extract($options);
			return "drop tables ".implode(",",$tables);
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryDropColumn( $options ) {
			extract($options);
			return "ALTER TABLE `$table` DROP `$property`";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryTestColumn( $options ) {
			extract($options);
			return "alter table `$table` add __test  ".$type;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryUpdateTest( $options ) {
			extract($options);
			return "update `$table` set __test=`$col`";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryMeasure( $options ) {
			extract($options);
			return "select count(*) as df from `$table` where
				strcmp(`$col`,__test) != 0 AND `$col` IS NOT NULL";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryRemoveTest($options) {
			extract($options);
			return "alter table `$table` change `$col` `$col` ".$type;
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getQueryDropTest($options) {
			extract($options);
			return "alter table `$table` drop __test";
		}


		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getIndex1($options) {
			extract($options);
			return "ALTER IGNORE TABLE `$table` ADD INDEX $indexname (`$col`)";
		}

		/**
		 *
		 * @param $options
		 * @return string $query
		 */
		private function getIndex2($options) {
			extract($options);
			return "ALTER IGNORE TABLE `$table` DROP INDEX $indexname";
		}

		/**
		 * Get SQL for destructors
		 * @param $options
		 * @return string $query
		 */
		private function getDestruct($options) {
			extract($options);
			if ($rollback) return;
			if ($engine=="innodb") return "COMMIT"; else return "";
		}

		/**
		 * Gets a basic SQL query
		 * @param array $options
		 * @param string $sql_type
		 * @return string $sql
		 */
		private function getBasicQuery( $options, $sql_type="SELECT" ) {
			extract($options);
			if (isset($fields)){
				$sqlfields = array();
				foreach($fields as $field) {
					$sqlfields[] = " `$field` ";
				}
				$field = implode(",", $fields);
			}
			if (!isset($field)) $field="";
			$sql = "$sql_type ".$field." FROM `$table` ";
			if (isset($where)) {
				if (is_array($where)) {
					$crit = array();
					foreach($where as $w=>$v) {
						$crit[] = " `$w` = \"".$v."\"";
					}
					$sql .= " WHERE ".implode(" AND ",$crit);
				}
				else {
					$sql .= " WHERE ".$where;
				}
			}
			return $sql;
		}




		/**
		 * (non-PHPdoc)
		 * @see RedBean/QueryWriter#getQuery()
		 */
		private function getQuery( $queryname, $params=array() ) {
			//echo "<br><b style='color:yellow'>$queryname</b>";
			switch($queryname) {
				case "create_table":
					return $this->getQueryCreateTable($params);
					break;
				case "widen_column":
					return $this->getQueryWiden($params);
					break;
				case "add_column":
					return $this->getQueryAddColumn($params);
					break;
				case "update":
					return $this->getQueryUpdate($params);
					break;
				case "insert":
					return $this->getQueryInsert($params);
					break;
				case "create":
					return $this->getQueryCreate($params);
					break;
				case "infertype":
					return $this->getQueryInferType($params);
					break;
				case "readtype":
		 			return $this->getBasicQuery(
		 				array("fields"=>array("tinyintus","intus","ints","varchar255","text"),
		 					"table" =>"dtyp",
		 					"where"=>array("id"=>$params["id"])));
		 			break;
				case "reset_dtyp":
					return $this->getQueryResetDTYP();
					break;
				case "prepare_innodb":
					return "SET autocommit=0";
					break;
				case "prepare_myisam":
					return "SET autocommit=1";
					break;
				case "starttransaction":
					return "START TRANSACTION";
					break;
				case "setup_dtyp":
					$engine = RedBean_OODB::getInstance()->getEngine();
					return "
				CREATE TABLE IF NOT EXISTS `dtyp` (
				  `id` int(11) unsigned NOT NULL auto_increment,
				  `tinyintus` tinyint(3) unsigned NOT NULL,
				  `intus` int(11) unsigned NOT NULL,
				  `ints` bigint(20) NOT NULL,
				  `varchar255` varchar(255) NOT NULL,
				  `text` text NOT NULL,
				  PRIMARY KEY  (`id`)
				) ENGINE=$engine DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
				";
					break;
				case "clear_dtyp":
					return "drop tables dtyp";
					break;
				case "setup_locking":
					$engine = RedBean_OODB::getInstance()->getEngine();
					return "
				CREATE TABLE IF NOT EXISTS `locking` (
				  `tbl` varchar(255) NOT NULL,
				  `id` bigint(20) NOT NULL,
				  `fingerprint` varchar(255) NOT NULL,
				  `expire` int(11) NOT NULL,
				  UNIQUE KEY `tbl` (`tbl`,`id`)
				) ENGINE=$engine DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
				";
					break;
				case "setup_tables":
					$engine = RedBean_OODB::getInstance()->getEngine();
					return "
				 CREATE TABLE IF NOT EXISTS `redbeantables` (
				 `id` INT( 11 ) UNSIGNED NOT NULL AUTO_INCREMENT ,
				 `tablename` VARCHAR( 255 ) NOT NULL ,
				 PRIMARY KEY ( `id` ),
				 UNIQUE KEY `tablename` (`tablename`)
				 ) ENGINE = $engine DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
				";
					break;
				case "show_tables":
					return "show tables";
					break;
				case "show_rtables":
					return "select tablename from redbeantables";
					break;
				case "register_table":
					return $this->getQueryRegisterTable( $params );
					break;
				case "unregister_table":
					return $this->getQueryUnregisterTable( $params );
					break;
				case "release":
					return $this->getQueryRelease( $params );
					break;
				case "remove_expir_lock":
					return $this->getQueryRemoveExpirLock( $params );
					break;
				case "update_expir_lock":
					return $this->getQueryUpdateExpirLock( $params );
					break;
				case "aq_lock":
					return $this->getQueryAQLock( $params );
					break;
				case "get_lock":
					return $this->getBasicQuery(array("fields"=>array("id"),"table"=>"locking","where"=>array("id"=>$params["id"],"tbl"=>$params["table"],"fingerprint"=>$params["key"])));
					break;
				case "get_bean":
					return $this->getBasicQuery(array("field"=>"*","table"=>$params["type"],"where"=>array("id"=>$params["id"])));
					break;
				case "bean_exists":
					return $this->getBasicQuery(array("field"=>"count(*)","table"=>$params["type"],"where"=>array("id"=>$params["id"])));
					break;
				case "count":
					return $this->getBasicQuery(array("field"=>"count(*)","table"=>$params["type"]));
					break;
				case "distinct":
					return $this->getQueryDistinct($params);
					break;
				case "stat":
					return $this->getBasicQuery(array("field"=>$params["stat"]."(`".$params["field"]."`)","table"=>$params["type"]));
					break;
				case "releaseall":
					return "TRUNCATE locking";
					break;
				case "fastload":
					return $this->getQueryFastLoad($params);
					break;
				case "where":
					return $this->getQueryWhere($params);
					break;
				case "find":
					return $this->getQueryFind( $params);
					break;
				case "list":
					return $this->getQueryList( $params);
					break;
				case "create_assoc":
					return $this->getQueryCreateAssoc( $params );
					break;
				case "add_assoc":
					return $this->getQueryAddAssoc( $params );
					break;
				case "add_assoc_now":
					return $this->getQueryAddAssocNow( $params );
					break;
				case "unassoc":
					return $this->getQueryUnassoc( $params );
					break;
				case "untree":
					return $this->getQueryUntree( $params );
					break;
				case "get_assoc":
					$col = $params["t1"]."_id";
					return $this->getBasicQuery(array(
						"table"=>$params["assoctable"],
						"fields"=>array( $params["t2"]."_id" ),
						"where"=>array( $col=>$params["id"])
					));
					break;
				case "trash":
					return $this->getBasicQuery(array("table"=>$params["table"],"where"=>array("id"=>$params["id"])),"DELETE");
					break;
				case "deltree":
					return $this->getBasicQuery(array("table"=>$params["table"],"where"=>" parent_id = ".$params["id"]." OR child_id = ".$params["id"]),"DELETE");
					break;
				case "unassoc_all_t1":
					$col = $params["t"]."_id";
					return $this->getBasicQuery(array("table"=>$params["table"],"where"=>array($col=>$params["id"])),"DELETE");
					break;
				case "unassoc_all_t2":
					$col = $params["t"]."2_id";
					return $this->getBasicQuery(array("table"=>$params["table"],"where"=>array($col=>$params["id"])),"DELETE");
					break;
				case "deltreetype":
					return $this->getQueryDeltreeType( $params );
					break;
				case "unassoctype1":
					$col = $params["t1"]."_id";
					$r = $this->getBasicQuery(array("table"=>$params["assoctable"],"where"=>array($col=>$params["id"])),"DELETE");
					//echo "<hr>$r";
					return $r;
					break;
				case "unassoctype2":
					$col = $params["t1"]."2_id";
					$r =$this->getBasicQuery(array("table"=>$params["assoctable"],"where"=>array($col=>$params["id"])),"DELETE");
					//echo "<hr>$r";
					return $r;
					break;
				case "create_tree":
					return $this->getQueryCreateTree( $params );
					break;
				case "unique":
					return $this->getQueryUnique( $params );
					break;
				case "add_child":
					return $this->getQueryAddChild( $params );
					break;
				case "get_children":
					return $this->getBasicQuery(array("table"=>$params["assoctable"],"fields"=>array("child_id"),
						"where"=>array("parent_id"=>$params["pid"])));
					break;
				case "get_parent":
					return $this->getBasicQuery(array( "where"=>array("child_id"=>$params["cid"]),"fields"=>array("parent_id"),"table"=>$params["assoctable"]	));
					break;
				case "remove_child":
					return $this->getQueryRemoveChild( $params );
					break;
				case "num_related":
					$col = $params["t1"]."_id";
					return $this->getBasicQuery(array("field"=>"COUNT(1)","table"=>$params["assoctable"],"where"=>array($col=>$params["id"])));
					break;
				case "drop_tables":
					return $this->getQueryDropTables( $params );
					break;
				case "truncate_rtables":
					return "truncate redbeantables";
					break;
				case "drop_column":
					return $this->getQueryDropColumn( $params );
					break;
				case "describe":
					return $this->getQueryDescribe( $params );
					break;
				case "get_null":
					return $this->getBasicQuery(array("field"=>"count(*)","table"=>$params["table"],"where"=>" `".$params["col"]."` IS NOT NULL "));
					return $this->getQueryGetNull( $params );
					break;
				case "test_column":
					return $this->getQueryTestColumn( $params );
					break;
				case "update_test":
					return $this->getQueryUpdateTest( $params );
					break;
				case "measure":
					return $this->getQueryMeasure( $params );
					break;
				case "remove_test":
					return $this->getQueryRemoveTest($params);
					break;
				case "drop_test":
					return $this->getQueryDropTest($params);
					break;
				case "variance":
					return $this->getBasicQuery(array("field"=>"count(distinct `".$params["col"]."`)","table"=>$params["table"]));
					break;
				case "index1":
					return $this->getIndex1($params);
					break;
				case "index2":
					return $this->getIndex2($params);
					break;
				case "drop_type":
					return $this->getBasicQuery(array("table"=>$params["type"]),"DELETE");
					break;
				case "destruct":
					return $this->getDestruct($params);
					break;
				default:
					throw new Exception("QueryWriter has no support for Query:".$queryname);
			}
		}

		/**
		 * @return string $query
		 */
		public function getQuote() {
			return "\"";
		}

		/**
		 * @return string $query
		 */
		public function getEscape() {
			return "`";
		}

                public function escape( $value ) {
                    return $this->adapter->escape( $value );
                }

                public function getTables() {
                    return $this->adapter->getCol( "show tables" );
                }


                public function createTable( $table ) {

                    $sql = "
                     CREATE TABLE `$table` (
                    `id` INT( 11 ) UNSIGNED NOT NULL AUTO_INCREMENT ,
                     PRIMARY KEY ( `id` )
                     ) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
                    ";
                    $this->adapter->exec( $sql );

		}

                public function getColumns( $table ) {

                    $columnsRaw = $this->adapter->get("DESCRIBE `$table`");

                    foreach($columnsRaw as $r) {
                       $columns[$r["Field"]]=$r["Type"];
                    }

                    return $columns;

                }

                public function scanType( $value ) {

                        $this->adapter->exec( $this->getQuery("reset_dtyp") );

			$checktypeSQL = $this->getQuery("infertype", array(
				"value"=> $this->escape(strval($value))
			));

			$this->adapter->exec( $checktypeSQL );
                        $id = $this->adapter->getInsertID();

			$readtypeSQL = $this->getQuery("readtype",array(
				"id"=>$id
			));

                        $row = $this->adapter->getRow($readtypeSQL);;

                        $tp = 0;
			foreach($row as $t=>$tv) {
				if (strval($tv) === strval($value)) {
					return $tp;
				}
				$tp++;
			}
			return $tp;
                }

                public function addColumn( $table, $column, $type ) {

                    $sql = $this->getQuery("add_column",array(
                              "table"=>$table,
                              "column"=>$column,
                              "type"=> $this->typeno_sqltype[$type]
                    ));

                    $this->adapter->exec( $sql );

                }

                public function code( $typedescription ) {
                    return $this->sqltype_typeno[$typedescription];
                }


                public function widenColumn( $table, $column, $type ) {

                       $changecolumnSQL = $this->getQuery( "widen_column", array(
                                "table" => $table,
                                "column" => $column,
                                "newtype" => $this->typeno_sqltype[$type]
                       ) );
                       $this->adapter->exec( $changecolumnSQL );

                }

                public function updateRecord( $table, $updatevalues, $id) {

                    $updateSQL = $this->getQuery("update", array(
                    "table"=>$table,
                    "updatevalues"=>$updatevalues,
                    "id"=>$id
                    ));

                    $this->adapter->exec( $updateSQL );
                }

                public function insertRecord( $table, $insertcolumns, $insertvalues ) {

                    if (count($insertvalues)>0){

                        $insertSQL = $this->getQuery("insert",array(
                          "table"=>$table,
                         "insertcolumns"=>$insertcolumns,
                         "insertvalues"=>$insertvalues
                        ));
                        $this->adapter->exec( $insertSQL );
                        return $this->adapter->getInsertID();
                    }
                    else {
                        $this->adapter->exec( $this->getQuery("create", array("table"=>$table)));
                        return $this->adapter->getInsertID();
                    }
                }


            public function selectRecord($type, $id) {
                $getSQL = $this->getQuery("get_bean",array(
                        "type"=>$type,
                        "id"=>$id
                ));
                $row = $this->adapter->getRow( $getSQL );

                if ($row && is_array($row) && count($row)>0) {
                    return $row;
                }
                else {
                    throw RedBean_Exception_Security("Could not find bean");
                }
           }

           public function deleteRecord( $table, $id ) {
               $this->adapter->exec("DELETE FROM `$table` WHERE id = $id ");
           }



}