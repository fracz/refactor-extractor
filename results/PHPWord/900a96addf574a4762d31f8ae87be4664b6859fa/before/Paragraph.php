<?php
/**
 * This file is part of PHPWord - A pure PHP library for reading and writing
 * word processing documents.
 *
 * PHPWord is free software distributed under the terms of the GNU Lesser
 * General Public License version 3 as published by the Free Software Foundation.
 *
 * For the full copyright and license information, please read the LICENSE
 * file that was distributed with this source code. For the full list of
 * contributors, visit https://github.com/PHPOffice/PHPWord/contributors.
 *
 * @link        https://github.com/PHPOffice/PHPWord
 * @copyright   2010-2014 PHPWord contributors
 * @license     http://www.gnu.org/licenses/lgpl.txt LGPL version 3
 */

namespace PhpOffice\PhpWord\Writer\Word2007\Style;

use PhpOffice\PhpWord\Style;
use PhpOffice\PhpWord\Style\Alignment as AlignmentStyle;

/**
 * Paragraph style writer
 *
 * @since 0.10.0
 */
class Paragraph extends AbstractStyle
{
    /**
     * Without w:pPr
     *
     * @var bool
     */
    private $withoutPPR = false;

    /**
     * Is inline in element
     *
     * @var bool
     */
    private $isInline = false;

    /**
     * Write style
     */
    public function write()
    {
        $xmlWriter = $this->getXmlWriter();

        $isStyleName = $this->isInline && !is_null($this->style) && is_string($this->style);
        if ($isStyleName) {
            if (!$this->withoutPPR) {
                $xmlWriter->startElement('w:pPr');
            }
            $xmlWriter->startElement('w:pStyle');
            $xmlWriter->writeAttribute('w:val', $this->style);
            $xmlWriter->endElement();
            if (!$this->withoutPPR) {
                $xmlWriter->endElement();
            }
        } else {
            $this->writeStyle();
        }
    }

    /**
     * Write full style
     */
    private function writeStyle()
    {
        $style = $this->getStyle();
        if (!$style instanceof \PhpOffice\PhpWord\Style\Paragraph) {
            return;
        }
        $xmlWriter = $this->getXmlWriter();

        if (!$this->withoutPPR) {
            $xmlWriter->startElement('w:pPr');
        }

        // Style name
        $styleName = $style->getStyleName();
        $xmlWriter->writeElementIf(!is_null($styleName), 'w:pStyle', 'w:val', $styleName);

        // Alignment
        $styleWriter = new Alignment($xmlWriter, new AlignmentStyle(array('value' => $style->getAlign())));
        $styleWriter->write();

        // Pagination
        $xmlWriter->writeElementIf(!$style->hasWidowControl(), 'w:widowControl', 'w:val', '0');
        $xmlWriter->writeElementIf($style->isKeepNext(), 'w:keepNext', 'w:val', '1');
        $xmlWriter->writeElementIf($style->isKeepLines(), 'w:keepLines', 'w:val', '1');
        $xmlWriter->writeElementIf($style->hasPageBreakBefore(), 'w:pageBreakBefore', 'w:val', '1');

        // Indentation
        $indentation = $style->getIndentation();
        if (!is_null($indentation)) {
            $styleWriter = new Indentation($xmlWriter, $indentation);
            $styleWriter->write();
        }

        // Spacing
        $spacing = $style->getSpace();
        if (!is_null($spacing)) {
            $styleWriter = new Spacing($xmlWriter, $spacing);
            $styleWriter->write();
        }

        // Tabs
        $tabs = $style->getTabs();
        if (!empty($tabs)) {
            $xmlWriter->startElement("w:tabs");
            foreach ($tabs as $tab) {
                $styleWriter = new Tab($xmlWriter, $tab);
                $styleWriter->write();
            }
            $xmlWriter->endElement();
        }

        // Numbering
        $numStyleName = $style->getNumStyle();
        $numStyleObject = Style::getStyle($numStyleName);
        if ($numStyleName !== null && $numStyleObject !== null) {
            $xmlWriter->startElement('w:numPr');
            $xmlWriter->startElement('w:numId');
            $xmlWriter->writeAttribute('w:val', $numStyleObject->getIndex());
            $xmlWriter->endElement(); // w:numId
            $xmlWriter->startElement('w:ilvl');
            $xmlWriter->writeAttribute('w:val', $style->getNumLevel());
            $xmlWriter->endElement(); // w:ilvl
            $xmlWriter->endElement(); // w:numPr

            $xmlWriter->startElement('w:outlineLvl');
            $xmlWriter->writeAttribute('w:val', $style->getNumLevel());
            $xmlWriter->endElement(); // w:outlineLvl
        }

        if (!$this->withoutPPR) {
            $xmlWriter->endElement(); // w:pPr
        }
    }

    /**
     * Set without w:pPr
     *
     * @param bool $value
     */
    public function setWithoutPPR($value)
    {
        $this->withoutPPR = $value;
    }

    /**
     * Set is inline
     *
     * @param bool $value
     */
    public function setIsInline($value)
    {
        $this->isInline = $value;
    }
}