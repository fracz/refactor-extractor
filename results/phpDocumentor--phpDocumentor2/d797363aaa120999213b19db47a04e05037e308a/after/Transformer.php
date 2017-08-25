<?php
/**
 * phpDocumentor
 *
 * PHP Version 5.3
 *
 * @copyright 2010-2013 Mike van Riel / Naenius (http://www.naenius.com)
 * @license   http://www.opensource.org/licenses/mit-license.php MIT
 * @link      http://phpdoc.org
 */

namespace phpDocumentor\Transformer;

use phpDocumentor\Descriptor\ProjectDescriptor;

/**
 * Core class responsible for transforming the cache file to a set of artifacts.
 */
class Transformer extends TransformerAbstract
{
    /** @var string|null $target Target location where to output the artifacts */
    protected $target = null;

    /** @var \phpDocumentor\Transformer\Template[] $templates */
    protected $templates = array();

    /** @var string $templates_path */
    protected $templates_path = '';

    /** @var Behaviour\Collection|null $behaviours */
    protected $behaviours = null;

    /** @var Writer\Collection $writer */
    protected $writers = null;

    /** @var Transformation[] $transformations */
    protected $transformations = array();

    /** @var boolean $parsePrivate */
    protected $parsePrivate = false;

    public function __construct(Writer\Collection $writer)
    {
        $this->writers = $writer;
    }

    /**
     * Array containing prefix => URL values.
     *
     * What happens is that the transformer knows where to find external API
     * docs for classes with a certain prefix.
     *
     * For example: having a prefix HTML_QuickForm2_ will link an unidentified
     * class that starts with HTML_QuickForm2_ to a (defined) URL
     * i.e. http://pear.php.net/package/HTML_QuickForm2/docs/
     * latest/HTML_QuickForm2/${class}.html
     *
     * @var string
     */
    protected $external_class_docs = array();

    /**
     * Sets the collection of behaviours that are applied before the actual transformation process.
     *
     * @param Behaviour\Collection $behaviours
     *
     * @return void
     */
    public function setBehaviours(Behaviour\Collection $behaviours)
    {
        $this->behaviours = $behaviours;
    }

    /**
     * Retrieves the collection of behaviours that should occur before the transformation process.
     *
     * @return Behaviour\Collection|null
     */
    public function getBehaviours()
    {
        return $this->behaviours;
    }

    /**
     * Sets the target location where to output the artifacts.
     *
     * @param string $target The target location where to output the artifacts.
     *
     * @throws \InvalidArgumentException if the target is not a valid writable
     *   directory.
     *
     * @return void
     */
    public function setTarget($target)
    {
        $path = realpath($target);
        if (!file_exists($path) && !is_dir($path) && !is_writable($path)) {
            throw new \InvalidArgumentException(
                'Given target directory (' . $target . ') does not exist or '
                . 'is not writable'
            );
        }

        $this->target = $path;
    }

    /**
     * Returns the location where to store the artifacts.
     *
     * @return string
     */
    public function getTarget()
    {
        return $this->target;
    }

    /**
     * Sets the path where the templates are located.
     *
     * @param string $path Absolute path where the templates are.
     *
     * @return void
     */
    public function setTemplatesPath($path)
    {
        $this->templates_path = $path;
    }

    /**
     * Returns the path where the templates are located.
     *
     * @return string
     */
    public function getTemplatesPath()
    {
        return $this->templates_path;
    }

    /**
     * Sets flag indicating whether private members and/or elements tagged
     * as {@internal} need to be displayed.
     *
     * @param bool $val True if all needs to be shown, false otherwise.
     *
     * @return void
     */
    public function setParseprivate($val)
    {
        $this->parsePrivate = (boolean)$val;
    }

    /**
     * Returns flag indicating whether private members and/or elements tagged
     * as {@internal} need to be displayed.
     *
     * @return bool
     */
    public function getParseprivate()
    {
        return $this->parsePrivate;
    }

    /**
     * Sets one or more templates as basis for the transformations.
     *
     * @param string|string[] $template Name or names of the templates.
     *
     * @return void
     */
    public function setTemplates($template)
    {
        $this->templates = array();

        if (!is_array($template)) {
            $template = array($template);
        }

        foreach ($template as $item) {
            $this->addTemplate($item);
        }
    }

    /**
     * Returns the list of templates which are going to be adopted.
     *
     * @return string[]
     */
    public function getTemplates()
    {
        return $this->templates;
    }

    /**
     * Loads a template by name, if an additional array with details is provided it will try to load parameters from it.
     *
     * @param string $name Name of the template to add.
     *
     * @return void
     */
    public function addTemplate($name)
    {
        // if the template is already loaded we do not reload it.
        if (isset($this->templates[$name])) {
            return;
        }

        $path = null;

        // if this is an absolute path; load the template into the configuration
        // Please note that this _could_ override an existing template when
        // you have a template in a subfolder with the same name as a default
        // template; we have left this in on purpose to allow people to override
        // templates should they choose to.
        $config_path = rtrim($name, DIRECTORY_SEPARATOR) . '/template.xml';
        if (file_exists($config_path) && is_readable($config_path)) {
            $path = rtrim($name, DIRECTORY_SEPARATOR);
            $template_name_part = basename($path);
            $cache_path = rtrim($this->getTemplatesPath(), '/\\')
            . DIRECTORY_SEPARATOR . $template_name_part;

            // move the files to a cache location and then change the path
            // variable to match the new location
            $this->copyRecursive($path, $cache_path);
            $path = $cache_path;

            // transform all directory separators to underscores and lowercase
            $name = strtolower(
                str_replace(
                    DIRECTORY_SEPARATOR,
                    '_',
                    rtrim($name, DIRECTORY_SEPARATOR)
                )
            );
        }

        // if we load a default template
        if ($path === null) {
            $path = rtrim($this->getTemplatesPath(), '/\\')
                    . DIRECTORY_SEPARATOR . $name;
        }

        if (!file_exists($path) || !is_readable($path)) {
            throw new \InvalidArgumentException(
                'The given template ' . $name.' could not be found or is not '
                . 'readable'
            );
        }

        // track templates to be able to refer to them later
        $this->templates[$name] = new Template($name, $path);
        $loader = new Template\XmlLoader($this, $this->writers);
        $loader->load($this->templates[$name], file_get_contents($path  . DIRECTORY_SEPARATOR . 'template.xml'));
    }

    /**
     * Returns the transformation which this transformer will process.
     *
     * @return Transformation[]
     */
    public function getTransformations()
    {
        $result = array();
        foreach ($this->templates as $template) {
            foreach ($template as $transformation) {
                $result[] = $transformation;
            }
        }

        return $result;
    }

    /**
     * Executes each transformation.
     *
     * @param ProjectDescriptor $project
     *
     * @return void
     */
    public function execute(ProjectDescriptor $project)
    {
        if ($this->getBehaviours() instanceof Behaviour\Collection) {
            $this->getBehaviours()->process($project);
        }

        foreach ($this->getTransformations() as $transformation) {
            $this->log(
                'Applying transformation'
                . ($transformation->getQuery() ? (' query "' . $transformation->getQuery() . '"') : '')
                . ' using writer ' . get_class($transformation->getWriter())
                . ' on '.$transformation->getArtifact()
            );

            $transformation->execute($project);
        }
    }

    /**
     * Converts a source file name to the name used for generating the end result.
     *
     * This method strips down the given $name using the following rules:
     *
     * * if the $name is postfixed with .php then that is removed
     * * any occurance of \ or DIRECTORY_SEPARATOR is replaced with .
     * * any dots that the name starts or ends with is removed
     * * the result is postfixed with .html
     *
     * @param string $name Name to convert.
     *
     * @return string
     */
    public function generateFilename($name)
    {
        if (substr($name, -4) == '.php') {
            $name = substr($name, 0, -4);
        }

        return trim(str_replace(array(DIRECTORY_SEPARATOR, '\\'), '.', trim($name, DIRECTORY_SEPARATOR . '.')), '.')
            . '.html';
    }

    /**
     * Copies a file or folder recursively to another location.
     *
     * @param string $src The source location to copy
     * @param string $dst The destination location to copy to
     *
     * @throws \Exception if $src does not exist or $dst is not writable
     *
     * @return void
     */
    public function copyRecursive($src, $dst)
    {
        // if $src is a normal file we can do a regular copy action
        if (is_file($src)) {
            copy($src, $dst);
            return;
        }

        $dir = opendir($src);
        if (!$dir) {
            throw new \Exception('Unable to locate path "' . $src . '"');
        }

        // check if the folder exists, otherwise create it
        if ((!file_exists($dst)) && (false === mkdir($dst))) {
            throw new \Exception('Unable to create folder "' . $dst . '"');
        }

        while (false !== ($file = readdir($dir))) {
            if (($file != '.') && ($file != '..')) {
                if (is_dir($src . '/' . $file)) {
                    $this->copyRecursive($src . '/' . $file, $dst . '/' . $file);
                } else {
                    copy($src . '/' . $file, $dst . '/' . $file);
                }
            }
        }
        closedir($dir);
    }

    /**
     * Adds a link to external documentation.
     *
     * Please note that the prefix string is matched against the
     * start of the class name and that the preceding \ for namespaces
     * should NOT be included.
     *
     * You can augment the URI with the name of the found class by inserting
     * the param {CLASS}. By default the class is inserted as-is; to insert a
     * lowercase variant use the parameter {LOWERCASE_CLASS}
     *
     * @param string $prefix Class prefix to match, i.e. Zend_Config_
     * @param string $uri    URI to link to when above prefix is encountered.
     *
     * @return void
     */
    public function setExternalClassDoc($prefix, $uri)
    {
        $this->external_class_docs[$prefix] = $uri;
    }

    /**
     * Sets a set of prefix -> url parts.
     *
     * @param string[] $external_class_docs Array containing prefix => URI pairs.
     *
     * @see self::setExternalClassDoc() for details on this feature.
     *
     * @return void
     */
    public function setExternalClassDocs($external_class_docs)
    {
        $this->external_class_docs = $external_class_docs;
    }

    /**
     * Returns the registered prefix -> url pairs.
     *
     * @return string[]
     */
    public function getExternalClassDocs()
    {
        return $this->external_class_docs;
    }

    /**
     * Retrieves the url for a given prefix.
     *
     * @param string $prefix Class prefix to retrieve a URL for.
     * @param string $class  If provided will replace the {CLASS} param with
     *  this string.
     *
     * @return string|null
     */
    public function getExternalClassDocumentLocation($prefix, $class = null)
    {
        if (!isset($this->external_class_docs[$prefix])) {
            return null;
        }

        $result = $this->external_class_docs[$prefix];
        if ($class !== null) {
            $result = str_replace(
                array('{CLASS}', '{LOWERCASE_CLASS}', '{UNPREFIXED_CLASS}'),
                array($class, strtolower($class), substr($class, strlen($prefix))),
                $result
            );
        }

        return $result;
    }

    /**
     * Returns the url for this class if it is registered.
     *
     * @param string $class FQCN to retrieve documentation URL for.
     *
     * @return null|string
     */
    public function findExternalClassDocumentLocation($class)
    {
        $class = ltrim($class, '\\');
        foreach (array_keys($this->external_class_docs) as $prefix) {
            if (strpos($class, $prefix) === 0) {
                return $this->getExternalClassDocumentLocation($prefix, $class);
            }
        }

        return null;
    }
}