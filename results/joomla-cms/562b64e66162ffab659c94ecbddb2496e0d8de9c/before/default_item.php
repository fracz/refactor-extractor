<?php
/**
 * @version		$Id$
 * @package		Joomla.Administrator
 * @subpackage	com_installer
 * @copyright	Copyright (C) 2005 - 2009 Open Source Matters, Inc. All rights reserved.
 * @license		GNU General Public License version 2 or later; see LICENSE.txt
 */

// no direct access
defined('_JEXEC') or die;
?>
<tr class="<?php echo "row".$this->item->index % 2; ?>" <?php echo $this->item->style; ?>>
	<td><?php echo $this->pagination->getRowOffset($this->item->index); ?></td>
	<td>
		<input type="checkbox" id="cb<?php echo $this->item->index;?>" name="eid[<?php echo $this->item->id; ?>]" value="<?php echo $this->item->client_id; ?>" onclick="isChecked(this.checked);" <?php echo $this->item->cbd; ?> />
		<span class="bold"><?php echo $this->item->name; ?></span>
	</td>
	<td class="center">
		<?php echo $this->item->client_id == "0" ? JText::_('Site') : JText::_('Admin'); ?>
	</td>
	<td class="center" <?php if (@$this->item->legacy) echo 'class="legacy-mode"'; ?>><?php echo @$this->item->version != '' ? $this->item->version : '&nbsp;'; ?></td>
	<td><?php echo @$this->item->creationdate != '' ? $this->item->creationdate : '&nbsp;'; ?></td>
	<td>
		<span class="editlinktip hasTip" title="<?php echo JText::_('Author Information');?>::<?php echo $this->item->author_information; ?>">
			<?php echo @$this->item->author != '' ? $this->item->author : '&nbsp;'; ?>
		</span>
	</td>
	<td class="center">
		<span class="editlinktip hasTip" title="<?php echo (@$this->item->legacy ? JText::_('Not Compatible Extension') : JText::_('Compatible Extension'));?>">
			<img src="templates/bluestork/admin/images/<?php echo (@$this->item->legacy ? 'publish_x.png' : 'tick.png');?>"/>
		</span>
	</td>
</tr>