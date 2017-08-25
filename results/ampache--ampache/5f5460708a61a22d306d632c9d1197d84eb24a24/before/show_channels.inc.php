<?php
/* vim:set softtabstop=4 shiftwidth=4 expandtab: */
/**
 *
 * LICENSE: GNU General Public License, version 2 (GPLv2)
 * Copyright 2001 - 2013 Ampache.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License v2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

$tags_list = Tag::get_display(Tag::get_tags());
?>
<?php if ($browse->get_show_header()) require AmpConfig::get('prefix') . '/templates/list_header.inc.php' ?>
<table class="tabledata" cellpadding="0" cellspacing="0">
    <tr class="th-top">
        <th class="cel_play"></th>
        <th class="cel_id"><?php echo Ajax::text('?page=browse&action=set_sort&browse_id=' . $browse->id . '&type=channel&sort=id', T_('#'),'channel_sort_id'); ?></th>
        <th class="cel_name"><?php echo Ajax::text('?page=browse&action=set_sort&browse_id=' . $browse->id . '&type=channel&sort=name', T_('Name'),'channel_sort_name'); ?></th>
        <th class="cel_interface"><?php echo Ajax::text('?page=browse&action=set_sort&browse_id=' . $browse->id . '&type=channel&sort=interface', T_('Interface'),'channel_sort_interface'); ?></th>
        <th class="cel_port"><?php echo Ajax::text('?page=browse&action=set_sort&browse_id=' . $browse->id . '&type=channel&sort=port', T_('Port'),'channel_sort_port'); ?></th>
        <th class="cel_data"><?php echo T_('Stream Source'); ?></th>
        <!--<th class="cel_random"><?php echo T_('Random'); ?></th>
        <th class="cel_loop"><?php echo T_('Loop'); ?></th>-->
        <th class="cel_streamtype"><?php echo T_('Stream Type'); ?></th>
        <th class="cel_bitrate"><?php echo T_('Bitrate'); ?></th>
        <th class="cel_startdate"><?php echo T_('Start Date'); ?></th>
        <th class="cel_listeners"><?php echo Ajax::text('?page=browse&action=set_sort&browse_id=' . $browse->id . '&type=channel&sort=listeners', T_('Listeners'),'channel_sort_listeners'); ?></th>
        <th class="cel_streamurl"><?php echo T_('Stream Url'); ?></th>
        <th class="cel_state"><?php echo T_('State'); ?></th>
        <th class="cel_action"><?php echo T_('Actions'); ?></th>
    </tr>
    <?php
    foreach ($object_ids as $channel_id) {
        $channel = new Channel($channel_id);
        $channel->format();
    ?>
    <tr class="<?php echo UI::flip_class(); ?>" id="channel_row_<?php echo $channel->id; ?>">
        <?php require AmpConfig::get('prefix') . '/templates/show_channel_row.inc.php'; ?>
    </tr>
    <?php } ?>
    <?php if (!count($object_ids)) { ?>
    <tr class="<?php echo UI::flip_class(); ?>">
        <td colspan="6"><span class="nodata"><?php echo T_('No channel found'); ?></span></td>
    </tr>
    <?php } ?>
</table>
<?php if ($browse->get_show_header()) require AmpConfig::get('prefix') . '/templates/list_header.inc.php' ?>