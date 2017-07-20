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
        $isStyleName = $this->isInline && !is_null($this->style) && is_string($this->style);
        if ($isStyleName) {
            if (!$this->withoutPPR) {
                $this->xmlWriter->startElement('w:pPr');
            }
            $this->xmlWriter->startElement('w:pStyle');
            $this->xmlWriter->writeAttribute('w:val', $this->style);
            $this->xmlWriter->endElement();
            if (!$this->withoutPPR) {
                $this->xmlWriter->endElement();
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
        if (!($this->style instanceof \PhpOffice\PhpWord\Style\Paragraph)) {
            return;
        }

        if (!$this->withoutPPR) {
            $this->xmlWriter->startElement('w:pPr');
        }

        // Alignment
        if (!is_null($this->style->getAlign())) {
            $this->xmlWriter->startElement('w:jc');
            $this->xmlWriter->writeAttribute('w:val', $this->style->getAlign());
            $this->xmlWriter->endElement();
        }

        // Indentation
        if (!is_null($this->style->getIndentation())) {
            $styleWriter = new Indentation($this->xmlWriter, $this->style->getIndentation());
            $styleWriter->write();
        }

        // Spacing
        if (!is_null($this->style->getSpace())) {
            $styleWriter = new Spacing($this->xmlWriter, $this->style->getSpace());
            $styleWriter->write();
        }

        // Pagination
        if (!$this->style->hasWidowControl()) {
            $this->xmlWriter->startElement('w:widowControl');
            $this->xmlWriter->writeAttribute('w:val', '0');
            $this->xmlWriter->endElement();
        }
        if ($this->style->isKeepNext()) {
            $this->xmlWriter->startElement('w:keepNext');
            $this->xmlWriter->writeAttribute('w:val', '1');
            $this->xmlWriter->endElement();
        }
        if ($this->style->isKeepLines()) {
            $this->xmlWriter->startElement('w:keepLines');
            $this->xmlWriter->writeAttribute('w:val', '1');
            $this->xmlWriter->endElement();
        }
        if ($this->style->hasPageBreakBefore()) {
            $this->xmlWriter->startElement('w:pageBreakBefore');
            $this->xmlWriter->writeAttribute('w:val', '1');
            $this->xmlWriter->endElement();
        }

        // Tabs
        $tabs = $this->style->getTabs();
        if (!empty($tabs)) {
            $this->xmlWriter->startElement("w:tabs");
            foreach ($tabs as $tab) {
                $styleWriter = new Tab($this->xmlWriter, $tab);
                $styleWriter->write();
            }
            $this->xmlWriter->endElement();
        }

        if (!$this->withoutPPR) {
            $this->xmlWriter->endElement(); // w:pPr
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