<?php
/**
 * @version		$Id: default_articles.php 16061 2010-04-14 05:04:48Z a.radtke $
 * @package		Joomla.Site
 * @subpackage	com_content
 * @copyright	Copyright (C) 2005 - 2010 Open Source Matters, Inc. All rights reserved.
 * @license		GNU General Public License version 2 or later; see LICENSE.txt
 */

// no direct access
defined('_JEXEC') or die;

JHtml::addIncludePath(JPATH_COMPONENT.DS.'helpers'.DS.'html');
JHtml::_('behavior.tooltip');
JHtml::core();

$n = count($this->items);

?>

<?php if (empty($this->items)) : ?>
	<p><?php echo JText::_('JContent_No_Articles'); ?></p>
<?php else : ?>
<form action="<?php echo JFilterOutput::ampReplace(JFactory::getURI()->toString()); ?>" method="post" name="adminForm">
	<?php if ($this->params->get('filter_field') != 'hide') :?>
	<fieldset class="filters">
	<legend class="element-invisible"><?php echo JText::_('JGLOBAL_FILTER_LABEL'); ?></legend>
		<div class="filter-search">
			<label class="filter-search-lbl" for="filter-search"><?php echo JText::_('Content_'.$this->params->get('filter_field').'_Filter_Label').'&nbsp;'; ?></label>
			<input type="text" name="filter-search" id="filter-search" value="<?php echo $this->escape($this->state->get('list.filter')); ?>" class="inputbox" onchange="document.adminForm.submit();" title="<?php echo JText::_('COM_CONTENT_FILTER_SEARCH_DESC'); ?>" />
		</div>
	<?php endif; ?>


	<?php if ($this->params->get('show_pagination_limit')) : ?>
		<div class="display-limit">
			<?php echo JText::_('COM_CONTENT_DISPLAY_NUM'); ?>&nbsp;
			<?php echo $this->pagination->getLimitBox(); ?>
		</div>
	<?php endif; ?>
	<?php if ($this->params->get('filter_field') != 'hide') :?>
	</fieldset>
	<?php endif; ?>

<table class="category" border="1">
	<?php if ($this->params->get('show_headings')) :?>
	<thead><tr>

	<?php //echo $this->params->get('list_show_title'); ?>
		<?php if ($this->params->get('list_show_title',1)) : ?>
		<th class="list-title" id="tableOrdering">
			<?php  echo JHTML::_('grid.sort', 'Content_Heading_Title', 'a.title', $this->state->get('list.direction'), $this->state->get('list.ordering')) ; ?>
		</th>
		<?php endif; ?>
		<?php if ($this->params->get('list_show_date',1)) : ?>
			<th class="list-date" id="tableOrdering2">
				<?php echo JHTML::_('grid.sort', 'Content_'.$this->params->get('show_date').'_Date', 'a.created', $this->state->get('list.direction'), $this->state->get('list.ordering')); ?>
			</th>
		<?php endif; ?>
		<?php if ($this->params->get('list_show_author',1)) : ?>
			<th class="list-author" id="tableOrdering3">
				<?php echo JHTML::_('grid.sort', 'JAUTHOR', 'author_name', $this->state->get('list.direction'), $this->state->get('list.ordering')); ?>
			</th>
		<?php endif; ?>
		<?php if ($this->params->get('list_show_hits',1)) : ?>
			<th class="list-hits" id="tableOrdering4">
				<?php echo JHTML::_('grid.sort', 'JGLOBAL_HITS', 'a.hits', $this->state->get('list.direction'), $this->state->get('list.ordering')); ?>
			</th>
		<?php endif; ?>
	</tr></thead>
	<?php endif; ?>
	<tbody>

		<?php foreach ($this->items as $i => &$article) : ?>
			<tr class="cat-list-row<?php echo $i % 2; ?>">

				<?php if (in_array($article->access, $this->user->authorisedLevels())) : ?>
				<?php if ($this->params->get('list_show_title',1)) : ?>
				<td class="list-title">
					<a href="<?php echo JRoute::_(ContentHelperRoute::getArticleRoute($article->slug, $article->catid)); ?>">
					<?php echo $this->escape($article->title); ?></a>
				</td>
				<?php endif; ?>
				<?php if ($this->params->get('list_show_date',1)) : ?>
					<td class="list-date">
						<?php echo JHTML::_('date',$article->displayDate, $this->escape(
						$this->params->get('date_format', JText::_('DATE_FORMAT_LC3')))); ?>
					</td>
				<?php endif; ?>
				<?php if ($this->params->get('list_show_author',1)) : ?>
					<td class="list-author">
						<?php echo $this->params->get('link_author', 0) ? JHTML::_('link',JRoute::_('index.php?option=com_users&view=profile&member_id='.$article->created_by),$article->author_name) : $article->author_name; ?>
					</td>
				<?php endif; ?>
				<?php if ($this->params->get('list_show_hits',1)) : ?>
					<td class="list-hits">
						<?php echo $article->hits; ?>
					</td>
				<?php endif; ?>
				<?php else : ?>
				<td>
					<?php
						echo $this->escape($article->title).' : ';
						$menu		= JSite::getMenu();
						$active		= $menu->getActive();
						$itemId		= $active->id;
						$link = JRoute::_('index.php?option=com_users&view=login&Itemid='.$itemId);
						$returnURL = JRoute::_(ContentHelperRoute::getArticleRoute($article->slug));
						$fullURL = new JURI($link);
						$fullURL->setVar('return', base64_encode($returnURL));
					?>
					<a href="<?php echo $fullURL; ?>" class="register">
					<?php echo JText::_( 'Register to read more...' ); ?></a>
				</td>
				<?php endif; ?>
			</tr>
		<?php endforeach; ?>
	</tbody>
	</table>

<?php if (($this->params->def('show_pagination', 2) == 1  || ($this->params->get('show_pagination') == 2)) && ($this->pagination->get('pages.total') > 1)) : ?>
	<div class="pagination">


		<?php if ($this->params->def('show_pagination_results', 1)) : ?>
         	<p class="counter">
                <?php echo $this->pagination->getPagesCounter(); ?>
        	</p>
        <?php  endif; ?>
				<?php echo $this->pagination->getPagesLinks(); ?>
	</div>
<?php endif; ?>

	<!-- @TODO add hidden inputs -->
	<input type="hidden" name="filter_order" value="" />
	<input type="hidden" name="filter_order_Dir" value="" />
	<input type="hidden" name="limitstart" value="" />
</form>
<?php endif; ?>



