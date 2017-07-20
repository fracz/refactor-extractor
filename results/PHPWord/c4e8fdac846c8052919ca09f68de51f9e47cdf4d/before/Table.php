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
 * Table style writer
 *
 * @since 0.10.0
 */
class Table extends AbstractStyle
{
    /**
     * Is full style
     *
     * @var bool
     */
    private $isFullStyle = true;

    /**
     * Write style
     */
    public function write()
    {
        if (!($this->style instanceof \PhpOffice\PhpWord\Style\Table)) {
            return;
        }

        $brdCol = $this->style->getBorderColor();
        $brdSz = $this->style->getBorderSize();
        $cellMargin = $this->style->getCellMargin();

        // If any of the borders/margins is set, process them
        $hasBorders = false;
        for ($i = 0; $i < 6; $i++) {
            if (!is_null($brdSz[$i])) {
                $hasBorders = true;
                break;
            }
        }
        $hasMargins = false;
        for ($i = 0; $i < 4; $i++) {
            if (!is_null($cellMargin[$i])) {
                $hasMargins = true;
                break;
            }
        }
        if ($hasMargins || $hasBorders) {
            $this->xmlWriter->startElement('w:tblPr');
            if ($hasMargins) {
                $mbWriter = new MarginBorder($this->xmlWriter);
                $mbWriter->setSizes($cellMargin);

                $this->xmlWriter->startElement('w:tblCellMar');
                $mbWriter->write();
                $this->xmlWriter->endElement(); // w:tblCellMar
            }
            if ($hasBorders) {
                $mbWriter = new MarginBorder($this->xmlWriter);
                $mbWriter->setSizes($brdSz);
                $mbWriter->setColors($brdCol);

                $this->xmlWriter->startElement('w:tblBorders');
                $mbWriter->write();
                $this->xmlWriter->endElement(); // w:tblBorders
            }
            $this->xmlWriter->endElement(); // w:tblPr
        }
        // Only write background color and first row for full style
        if ($this->isFullStyle) {
            // Background color
            if (!is_null($this->style->getShading())) {
                $this->xmlWriter->startElement('w:tcPr');
                $styleWriter = new Shading($this->xmlWriter, $this->style->getShading());
                $styleWriter->write();
                $this->xmlWriter->endElement();
            }
            // First Row
            $firstRow = $this->style->getFirstRow();
            if ($firstRow instanceof \PhpOffice\PhpWord\Style\Table) {
                $this->writeFirstRow($firstRow, 'firstRow');
            }
        }
    }

    /**
     * Set is full style
     *
     * @param bool $value
     */
    public function setIsFullStyle($value)
    {
        $this->isFullStyle = $value;
    }

    /**
     * Write row style
     *
     * @param string $type
     */
    private function writeFirstRow(\PhpOffice\PhpWord\Style\Table $style, $type)
    {
        $this->xmlWriter->startElement('w:tblStylePr');
        $this->xmlWriter->writeAttribute('w:type', $type);
        $this->xmlWriter->startElement('w:tcPr');
        if (!is_null($style->getShading())) {
            $styleWriter = new Shading($this->xmlWriter, $style->getShading());
            $styleWriter->write();
        }

        // Borders
        $brdSz = $style->getBorderSize();
        $brdCol = $style->getBorderColor();
        $hasBorders = false;
        for ($i = 0; $i < 6; $i++) {
            if (!is_null($brdSz[$i])) {
                $hasBorders = true;
            }
        }
        if ($hasBorders) {
            $mbWriter = new MarginBorder($this->xmlWriter);
            $mbWriter->setSizes($brdSz);
            $mbWriter->setColors($brdCol);

            $this->xmlWriter->startElement('w:tcBorders');
            $mbWriter->write();
            $this->xmlWriter->endElement(); // w:tcBorders
        }

        $this->xmlWriter->endElement(); // w:tcPr
        $this->xmlWriter->endElement(); // w:tblStylePr
    }
}