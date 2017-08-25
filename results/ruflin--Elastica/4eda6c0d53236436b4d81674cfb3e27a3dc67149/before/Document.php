<?php

namespace Elastica;

use Elastica\Exception\InvalidException;

/**
 * Single document stored in elastic search
 *
 * @category Xodoa
 * @package  Elastica
 * @author   Nicolas Ruflin <spam@ruflin.com>
 */
class Document extends Param
{
    /**
     * Document data
     *
     * @var array Document data
     */
    protected $_data = array();

    /**
     * Optype
     *
     * @var string Optype
     */
    protected $_optype = '';

    /**
     * Percolate
     *
     * @var string Percolate
     */
    protected $_percolate = '';

    /**
     * @var \Elastica\Script
     */
    protected $_script;

    /**
     * Creates a new document
     *
     * @param int|string $id    OPTIONAL $id Id is create if empty
     * @param array      $data  OPTIONAL Data array
     * @param string     $type  OPTIONAL Type name
     * @param string     $index OPTIONAL Index name
     */
    public function __construct($id = '', array $data = array(), $type = '', $index = '')
    {
        $this->setId($id);
        $this->setData($data);
        $this->setType($type);
        $this->setIndex($index);
    }

    /**
     * Sets the id of the document.
     *
     * @param  string            $id
     * @return \Elastica\Document
     */
    public function setId($id)
    {
        return $this->setParam('_id', $id);
    }

    /**
     * Returns document id
     *
     * @return string|int Document id
     */
    public function getId()
    {
        return ($this->hasParam('_id'))?$this->getParam('_id'):null;
    }

    /**
     * @return bool
     */
    public function hasId()
    {
        return '' !== (string) $this->getId();
    }

    /**
     * @param string $key
     * @return mixed
     */
    public function __get($key)
    {
        return $this->get($key);
    }

    /**
     * @param string $key
     * @param mixed $value
     */
    public function __set($key, $value)
    {
        $this->set($key, $value);
    }

    /**
     * @param string $key
     * @return bool
     */
    public function __isset($key)
    {
        return $this->has($key) && null !== $this->get($key);
    }

    /**
     * @param string $key
     */
    public function __unset($key)
    {
        $this->remove($key);
    }

    /**
     * @param string $key
     * @return mixed
     * @throws Exception\InvalidException
     */
    public function get($key)
    {
        if (!array_key_exists($key, $this->_data)) {
            throw new InvalidException("Field {$key} does not exist");
        }
        return $this->_data[$key];
    }

    /**
     * @param string $key
     * @param mixed $value
     * @return \Elastica\Document
     */
    public function set($key, $value)
    {
        $this->_data[$key] = $value;

        return $this;
    }

    /**
     * @param string $key
     * @return bool
     */
    public function has($key)
    {
        return array_key_exists($key, $this->_data);
    }

    /**
     * @param string $key
     * @throws Exception\InvalidException
     * @return \Elastica\Document
     */
    public function remove($key)
    {
        if (!array_key_exists($key, $this->_data)) {
            throw new InvalidException("Field {$key} does not exist");
        }
        unset($this->_data[$key]);

        return $this;
    }

    /**
     * Adds the given key/value pair to the document
     *
     * @deprecated
     * @param  string            $key   Document entry key
     * @param  mixed             $value Document entry value
     * @return \Elastica\Document
     */
    public function add($key, $value)
    {
        return $this->set($key, $value);
    }

    /**
     * Adds a file to the index
     *
     * To use this feature you have to call the following command in the
     * elasticsearch directory:
     * <code>
     * ./bin/plugin -install elasticsearch/elasticsearch-mapper-attachments/1.6.0
     * </code>
     * This installs the tika file analysis plugin. More infos about supported formats
     * can be found here: {@link http://tika.apache.org/0.7/formats.html}
     *
     * @param  string            $key      Key to add the file to
     * @param  string            $filepath Path to add the file
     * @param  string            $mimeType OPTIONAL Header mime type
     * @return \Elastica\Document
     */
    public function addFile($key, $filepath, $mimeType = '')
    {
        $value = base64_encode(file_get_contents($filepath));

        if (!empty($mimeType)) {
            $value = array('_content_type' => $mimeType, '_name' => $filepath, 'content' => $value,);
        }

        $this->set($key, $value);

        return $this;
    }

    /**
     * Add file content
     *
     * @param  string            $key     Document key
     * @param  string            $content Raw file content
     * @return \Elastica\Document
     */
    public function addFileContent($key, $content)
    {
        return $this->set($key, base64_encode($content));
    }

    /**
     * Adds a geopoint to the document
     *
     * Geohashes are not yet supported
     *
     * @param string $key       Field key
     * @param float  $latitude  Latitude value
     * @param float  $longitude Longitude value
     * @link http://www.elasticsearch.org/guide/reference/mapping/geo-point-type.html
     * @return \Elastica\Document
     */
    public function addGeoPoint($key, $latitude, $longitude)
    {
        $value = array('lat' => $latitude, 'lon' => $longitude,);

        $this->set($key, $value);

        return $this;
    }

    /**
     * Overwrites the current document data with the given data
     *
     * @param  array             $data Data array
     * @return \Elastica\Document
     */
    public function setData(array $data)
    {
        $this->_data = $data;

        return $this;
    }

    /**
     * Sets lifetime of document
     *
     * @param  string            $ttl
     * @return \Elastica\Document
     */
    public function setTtl($ttl)
    {
        return $this->set('_ttl', $ttl);
    }

    /**
     * Returns the document data
     *
     * @return array Document data
     */
    public function getData()
    {
        return $this->_data;
    }

    /**
     * Sets the document type name
     *
     * @param  string            $type Type name
     * @return \Elastica\Document Current object
     */
    public function setType($type)
    {
        if ($type instanceof Type) {
            $this->setIndex($type->getIndex());
            $type = $type->getName();
        }
        return $this->setParam('_type', $type);
    }

    /**
     * Return document type name
     *
     * @return string                              Document type name
     * @throws \Elastica\Exception\InvalidException
     */
    public function getType()
    {
       return $this->getParam('_type');
    }

    /**
     * Sets the document index name
     *
     * @param  string            $index Index name
     * @return \Elastica\Document Current object
     */
    public function setIndex($index)
    {
        if ($index instanceof Index) {
            $index = $index->getName();
        }
        return $this->setParam('_index', $index);
    }

    /**
     * Get the document index name
     *
     * @return string                              Index name
     * @throws \Elastica\Exception\InvalidException
     */
    public function getIndex()
    {
        return $this->getParam('_index');
    }

    /**
     * Sets the version of a document for use with optimistic concurrency control
     *
     * @param  int               $version Document version
     * @return \Elastica\Document Current object
     * @link http://www.elasticsearch.org/blog/2011/02/08/versioning.html
     */
    public function setVersion($version)
    {
        return $this->setParam('_version', (int) $version);
    }

    /**
     * Returns document version
     *
     * @return string|int Document version
     */
    public function getVersion()
    {
        return $this->getParam('_version');
    }

    /**
     * @return bool
     */
    public function hasVersion()
    {
        return $this->hasParam('_version');
    }

    /**
     * Sets the version_type of a document
     * Default in ES is internal, but you can set to external to use custom versioning
     *
     * @param  int               $versionType Document version type
     * @return \Elastica\Document Current object
     * @link http://www.elasticsearch.org/guide/reference/api/index_.html
     */
    public function setVersionType($versionType)
    {
        return $this->setParam('_version_type', $versionType);
    }

    /**
     * Returns document version type
     *
     * @return string|int Document version type
     */
    public function getVersionType()
    {
        return $this->getParam('_version_type');
    }

    /**
     * @return bool
     */
    public function hasVersionType()
    {
        return $this->hasParam('_version_type');
    }

    /**
     * Sets parent document id
     *
     * @param  string|int        $parent Parent document id
     * @return \Elastica\Document Current object
     * @link http://www.elasticsearch.org/guide/reference/mapping/parent-field.html
     */
    public function setParent($parent)
    {
        return $this->setParam('_parent', $parent);
    }

    /**
     * Returns the parent document id
     *
     * @return string|int Parent document id
     */
    public function getParent()
    {
        return $this->getParam('_parent');
    }

    /**
     * @return bool
     */
    public function hasParent()
    {
        return $this->hasParam('_parent');
    }

    /**
     * Set operation type
     *
     * @param  string            $optype Only accept create
     * @return \Elastica\Document Current object
     */
    public function setOpType($optype)
    {
        $this->_optype = $optype;

        return $this;
    }

    /**
     * Get operation type
     */
    public function getOpType()
    {
        return $this->_optype;
    }

    /**
     * Set percolate query param
     *
     * @param  string            $value percolator filter
     * @return \Elastica\Document
     */
    public function setPercolate($value = '*')
    {
        $this->_percolate = $value;

        return $this;
    }

    /**
     * Get percolate parameter
     *
     * @return string
     */
    public function getPercolate()
    {
        return $this->_percolate;
    }

    /**
     * Set routing query param
     *
     * @param  string            $value routing
     * @return \Elastica\Document
     */
    public function setRouting($value)
    {
        return $this->setParam('_routing', $value);
    }

    /**
     * Get routing parameter
     *
     * @return string
     */
    public function getRouting()
    {
        return $this->getParam('_routing');
    }

    /**
     * @return bool
     */
    public function hasRouting()
    {
        return $this->hasParam('_routing');
    }

    /**
     * @param array $fields
     * @return \Elastica\Document
     */
    public function setFields(array $fields)
    {
        return $this->setParam('_fields', $fields);
    }

    /**
     * @param string $field
     * @return \Elastica\Document
     */
    public function addField($field)
    {
        return $this->addParam('_fields', $field);
    }

    /**
     * @return \Elastica\Document
     */
    public function setFieldsSource()
    {
        return $this->setFields(array('_source'));
    }

    /**
     * @return array
     */
    public function getFields()
    {
        return $this->getParam('_fields');
    }

    /**
     * @return bool
     */
    public function hasFields()
    {
        return $this->hasParam('_fields');
    }

    /**
     * @param int $num
     * @return Param
     */
    public function setRetryOnConflict($num)
    {
        return $this->setParam('_retry_on_conflict', (int) $num);
    }

    /**
     * @return int
     */
    public function getRetryOnConflict()
    {
        return $this->getParam('_retry_on_conflict');
    }

    /**
     * @return bool
     */
    public function hasRetryOnConflict()
    {
        return $this->hasParam('_retry_on_conflict');
    }

    /**
     * @param bool $forUpdate
     * @return array
     */
    public function getOptions($forUpdate = false)
    {
        $options = array();

        if ($this->hasVersion()) {
            $options['version'] = $this->getVersion();
        }

        if ($this->hasVersionType()) {
            $options['version_type'] = $this->getVersionType();
        }

        if ($this->hasParent()) {
            $options['parent'] = $this->getParent();
        }

        if ($this->getOpType()) {
            $options['op_type'] = $this->getOpType();
        }

        if ($this->getPercolate()) {
            $options['percolate'] = $this->getPercolate();
        }

        if ($this->hasRouting()) {
            $options['routing'] = $this->getRouting();
        }

        if ($forUpdate) {

            if ($this->hasFields()) {
                $options['fields'] = implode(',', $this->getFields());
            }

            if ($this->hasRetryOnConflict()) {
                $options['retry_on_conflict'] = $this->getRetryOnConflict();
            }

        }

        return $options;
    }

    /**
     * @param \Elastica\Script|array|string $data
     * @return $this
     */
    public function setScript($data)
    {
        $script = Script::create($data);
        $this->_script = $script;

        return $this;
    }

    /**
     * @return \Elastica\Script
     */
    public function getScript()
    {
        return $this->_script;
    }

    /**
     * @return bool
     */
    public function hasScript()
    {
        return null !== $this->_script;
    }

    /**
     * Returns the document as an array
     * @return array
     */
    public function toArray()
    {
        $doc = $this->getParams();
        $doc['_source'] = $this->getData();

        return $doc;
    }
}