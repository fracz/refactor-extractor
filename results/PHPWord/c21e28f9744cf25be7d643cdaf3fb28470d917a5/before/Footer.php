<?php
/**
 * PHPWord
 *
 * @link        https://github.com/PHPOffice/PHPWord
 * @copyright   2014 PHPWord
 * @license     http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt LGPL
 */

namespace PhpOffice\PhpWord\Element;

use PhpOffice\PhpWord\Element\Table;

/**
 * Footer element
 */
class Footer extends AbstractContainer
{
    /**
     * Header/footer types constants
     *
     * @var string
     * @link http://www.schemacentral.com/sc/ooxml/a-wtype-4.html Header or Footer Type
     */
    const AUTO  = 'default';  // default and odd pages
    const FIRST = 'first';
    const EVEN  = 'even';

    /**
     * Container type
     *
     * @var string
     */
    protected $container = 'footer';

    /**
     * Header type
     *
     * @var string
     */
    protected $type = self::AUTO;

    /**
     * Create new instance
     *
     * @param int $sectionId
     * @param int $footerId
     * @param string $type
     */
    public function __construct($sectionId, $containerId = 1, $type = self::AUTO)
    {
        $this->sectionId = $sectionId;
        $this->setType($type);
        $this->setDocPart($this->container, ($sectionId - 1) * 3 + $containerId);
    }

    /**
     * Set type
     *
     * @param string $value
     * @since 0.10.0
     */
    public function setType($value = self::AUTO)
    {
        if (!in_array($value, array(self::AUTO, self::FIRST, self::EVEN))) {
            $value = self::AUTO;
        }
        $this->type = $value;
    }

    /**
     * Get type
     *
     * @return string
     * @since 0.10.0
     */
    public function getType()
    {
        return $this->type;
    }

    /**
     * Reset type to default
     *
     * @return string
     */
    public function resetType()
    {
        return $this->type = self::AUTO;
    }

    /**
     * First page only header
     *
     * @return string
     */
    public function firstPage()
    {
        return $this->type = self::FIRST;
    }

    /**
     * Even numbered pages only
     *
     * @return string
     */
    public function evenPage()
    {
        return $this->type = self::EVEN;
    }

    /**
     * Add table element
     *
     * @param mixed $style
     * @return \PhpOffice\PhpWord\Element\Table
     * @todo Merge with the same function on Section
     */
    public function addTable($style = null)
    {
        $table = new Table($this->getDocPart(), $this->getDocPartId(), $style);
        $this->addElement($table);

        return $table;
    }
}