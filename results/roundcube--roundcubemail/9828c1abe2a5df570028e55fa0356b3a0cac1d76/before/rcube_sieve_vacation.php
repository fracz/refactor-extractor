<?php

/**
 * Managesieve Vacation Engine
 *
 * Engine part of Managesieve plugin implementing UI and backend access.
 *
 * Copyright (C) 2011-2014, Kolab Systems AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

class rcube_sieve_vacation extends rcube_sieve_engine
{
    function actions()
    {
        $error = $this->start('vacation');

        // find current vacation rule
        if (!$error) {
            $this->vacation_rule();
            $this->vacation_post();
        }
        $this->plugin->add_label('vacation.saving');
        $this->rc->output->add_handlers(array(
            'vacationform' => array($this, 'vacation_form'),
        ));

        $this->rc->output->set_pagetitle($this->plugin->gettext('vacation'));
        $this->rc->output->send('managesieve.vacation');
    }

    private function vacation_rule()
    {
        $this->vacation = array();

        if (empty($this->active)) {
            return;
        }

        $list = array();

        // find (first) vacation rule
        foreach ($this->script as $idx => $rule) {
            if (empty($this->vacation) && !empty($rule['actions']) && $rule['actions'][0]['type'] == 'vacation') {
                foreach ($rule['actions'] as $act) {
                    if ($act['type'] == 'discard' || $act['type'] == 'keep') {
                        $action = $act['type'];
                    }
                    else if ($act['type'] == 'redirect') {
                        $action = $act['copy'] ? 'copy' : 'redirect';
                        $target = $act['target'];
                    }
                }

                $this->vacation = array_merge($rule['actions'][0], array(
                        'idx'      => $idx,
                        'disabled' => $rule['disabled'],
                        'name'     => $rule['name'],
                        'tests'    => $rule['tests'],
                        'action'   => $action ?: 'keep',
                        'target'   => $target,
                ));
            }
            else {
                $list[$idx] = $rule['name'];
            }
        }

        $this->vacation['list'] = $list;
    }

    private function vacation_post()
    {
        if (empty($_POST)) {
            return;
        }

        $date_extension  = in_array('date', $this->exts);
        $regex_extension = in_array('regex', $this->exts);

        $status        = rcube_utils::get_input_value('vacation_status', rcube_utils::INPUT_POST);
        $subject       = rcube_utils::get_input_value('vacation_subject', rcube_utils::INPUT_POST, true);
        $reason        = rcube_utils::get_input_value('vacation_reason', rcube_utils::INPUT_POST, true);
        $addresses     = rcube_utils::get_input_value('vacation_addresses', rcube_utils::INPUT_POST, true);
        $interval      = rcube_utils::get_input_value('vacation_interval', rcube_utils::INPUT_POST);
        $interval_type = rcube_utils::get_input_value('vacation_interval_type', rcube_utils::INPUT_POST);
        $date_from     = rcube_utils::get_input_value('vacation_datefrom', rcube_utils::INPUT_POST);
        $date_to       = rcube_utils::get_input_value('vacation_dateto', rcube_utils::INPUT_POST);
        $after         = rcube_utils::get_input_value('vacation_after', rcube_utils::INPUT_POST);
        $action        = rcube_utils::get_input_value('vacation_action', rcube_utils::INPUT_POST);
        $target        = rcube_utils::get_input_value('action_target', rcube_utils::INPUT_POST, true);
        $target_domain = rcube_utils::get_input_value('action_domain', rcube_utils::INPUT_POST);

        $interval_type                   = $interval_type == 'seconds' ? 'seconds' : 'days';
        $vacation_action['type']         = 'vacation';
        $vacation_action['reason']       = $this->strip_value(str_replace("\r\n", "\n", $reason));
        $vacation_action['subject']      = $subject;
        $vacation_action['addresses']    = $addresses;
        $vacation_action[$interval_type] = $interval;
        $vacation_tests                  = (array) $this->vacation['tests'];

        foreach ((array) $vacation_action['addresses'] as $aidx => $address) {
            $vacation_action['addresses'][$aidx] = $address = trim($address);

            if (empty($address)) {
                unset($vacation_action['addresses'][$aidx]);
            }
            else if (!rcube_utils::check_email($address)) {
                $error = 'noemailwarning';
                break;
            }
        }

        if ($vacation_action['reason'] == '') {
            $error = 'managesieve.cannotbeempty';
        }
        if ($vacation_action[$interval_type] && !preg_match('/^[0-9]+$/', $vacation_action[$interval_type])) {
            $error = 'managesieve.forbiddenchars';
        }

        // find and remove existing date/regex/true rules
        foreach ((array) $vacation_tests as $idx => $t) {
            if (($t['test'] == 'currentdate' && $t['part'] == 'date' && $t['type'] == $type)
                || ($t['test'] == 'header' && $t['type'] == 'regex' && $t['arg1'] == 'received')
                || ($t['test'] == 'true')
            ) {
                unset($vacation_tests[$idx]);
            }
        }

        if ($date_extension) {
            foreach (array('date_from', 'date_to') as $var) {
                $date = $$var;
                if ($date && ($dt = rcube_utils::anytodatetime($date))) {
                    $type = 'value-' . ($var == 'date_from' ? 'ge' : 'le');
                    $test = array(
                        'test' => 'currentdate',
                        'part' => 'date',
                        'type' => $type,
                        'arg'  => $dt->format('Y-m-d'),
                    );

                    $vacation_tests[] = $test;
                }
            }
        }
        else if ($regex_extension) {
            // Add date range rules if range specified
            if ($date_from && $date_to) {
                if ($tests = self::build_regexp_tests($date_from, $date_to, $error)) {
                    $vacation_tests = array_merge($vacation_tests, $tests);
                }
            }
        }

        if ($action == 'redirect' || $action == 'copy') {
            if ($target_domain) {
                $target .= '@' . $target_domain;
            }

            if (empty($target) || !rcube_utils::check_email($target)) {
                $error = 'noemailwarning';
            }
        }

        if (empty($vacation_tests)) {
            $vacation_tests = $this->rc->config->get('managesieve_vacation_test', array(array('test' => 'true')));
        }

        // @TODO: handle situation when there's no active script

        if (!$error) {
            $rule               = $this->vacation;
            $rule['type']       = 'if';
            $rule['name']       = $rule['name'] ?: $this->plugin->gettext('vacation');
            $rule['disabled']   = $status == 'off';
            $rule['tests']      = $vacation_tests;
            $rule['join']       = $date_extension ? count($vacation_tests) > 1 : false;
            $rule['actions']    = array($vacation_action);

            if ($action && $action != 'keep') {
                $rule['actions'][] = array(
                    'type'   => $action == 'discard' ? 'discard' : 'redirect',
                    'copy'   => $action == 'copy',
                    'target' => $action != 'discard' ? $target : '',
                );
            }

            // reset original vacation rule
            if (isset($this->vacation['idx'])) {
                $this->script[$this->vacation['idx']] = null;
            }

            // re-order rules if needed
            if (isset($after) && $after !== '') {
                // add at target position
                if ($after >= count($this->script) - 1) {
                    $this->script[] = $rule;
                }
                else {
                    $script = array();

                    foreach ($this->script as $idx => $r) {
                        if ($r) {
                            $script[] = $r;
                        }

                        if ($idx == $after) {
                            $script[] = $rule;
                        }
                    }

                    $this->script = $script;
                }
            }
            else {
                array_unshift($this->script, $rule);
            }

            $this->sieve->script->content = array_values(array_filter($this->script));

            if ($this->save_script()) {
                $this->rc->output->show_message('managesieve.vacationsaved', 'confirmation');
                $this->rc->output->send();
            }
        }

        $this->rc->output->show_message($error ? $error : 'managesieve.saveerror', 'error');
        $this->rc->output->send();
    }

    /**
     * Independent vacation form
     */
    public function vacation_form($attrib)
    {
        // check supported extensions
        $date_extension    = in_array('date', $this->exts);
        $regex_extension   = in_array('regex', $this->exts);
        $seconds_extension = in_array('vacation-seconds', $this->exts);

        // build FORM tag
        $form_id = !empty($attrib['id']) ? $attrib['id'] : 'form';
        $out     = $this->rc->output->request_form(array(
            'id'      => $form_id,
            'name'    => $form_id,
            'method'  => 'post',
            'task'    => 'settings',
            'action'  => 'plugin.managesieve-vacation',
            'noclose' => true
            ) + $attrib);

        // form elements
        $subject   = new html_inputfield(array('name' => 'vacation_subject', 'id' => 'vacation_subject', 'size' => 50));
        $reason    = new html_textarea(array('name' => 'vacation_reason', 'id' => 'vacation_reason', 'cols' => 60, 'rows' => 8));
        $interval  = new html_inputfield(array('name' => 'vacation_interval', 'id' => 'vacation_interval', 'size' => 5));
        $addresses = '<textarea name="vacation_addresses" id="vacation_addresses" data-type="list" data-size="30" style="display: none">'
            . rcube::Q(implode("\n", (array) $this->vacation['addresses']), 'strict', false) . '</textarea>';
        $status    = new html_select(array('name' => 'vacation_status', 'id' => 'vacation_status'));
        $action    = new html_select(array('name' => 'vacation_action', 'id' => 'vacation_action', 'onchange' => 'vacation_action_select()'));

        $status->add($this->plugin->gettext('vacation.on'), 'on');
        $status->add($this->plugin->gettext('vacation.off'), 'off');

        $action->add($this->plugin->gettext('vacation.keep'), 'keep');
        $action->add($this->plugin->gettext('vacation.discard'), 'discard');
        $action->add($this->plugin->gettext('vacation.redirect'), 'redirect');
        if (in_array('copy', $this->exts)) {
            $action->add($this->plugin->gettext('vacation.copy'), 'copy');
        }

        if ($this->rc->config->get('managesieve_vacation') != 2 && count($this->vacation['list'])) {
            $after = new html_select(array('name' => 'vacation_after', 'id' => 'vacation_after'));

            $after->add('', '');
            foreach ($this->vacation['list'] as $idx => $rule) {
                $after->add($rule, $idx);
            }
        }

        $interval_txt = $interval->show(isset($this->vacation['seconds']) ? $this->vacation['seconds'] : $this->vacation['days']);
        if ($seconds_extension) {
            $interval_select = new html_select(array('name' => 'vacation_interval_type'));
            $interval_select->add($this->plugin->gettext('days'), 'days');
            $interval_select->add($this->plugin->gettext('seconds'), 'seconds');
            $interval_txt .= '&nbsp;' . $interval_select->show(isset($this->vacation['seconds']) ? 'seconds' : 'days');
        }
        else {
            $interval_txt .= '&nbsp;' . $this->plugin->gettext('days');
        }

        if ($date_extension || $regex_extension) {
            $date_from   = new html_inputfield(array('name' => 'vacation_datefrom', 'id' => 'vacation_datefrom', 'class' => 'datepicker', 'size' => 12));
            $date_to     = new html_inputfield(array('name' => 'vacation_dateto', 'class' => 'datepicker', 'size' => 12));
            $date_format = $this->rc->config->get('date_format', 'Y-m-d');
        }

        if ($date_extension) {
            foreach ((array) $this->vacation['tests'] as $test) {
                if ($test['test'] == 'currentdate' && $test['part'] == 'date') {
                    $date = $this->rc->format_date($test['arg'], $date_format, false);
                    $date_value[$test['type'] == 'value-ge' ? 'from' : 'to'] = $date;
                }
            }
        }
        else if ($regex_extension) {
            // Sieve 'date' extension not available, read start/end from RegEx based rules instead
            if ($date_tests = self::parse_regexp_tests($this->vacation['tests'])) {
                $date_value['from'] = $this->rc->format_date($date_tests['from'], $date_format, false);
                $date_value['to']   = $this->rc->format_date($date_tests['to'], $date_format, false);
            }
        }

        // force domain selection in redirect email input
        $domains  = (array) $this->rc->config->get('managesieve_domains');
        $redirect = $this->vacation['action'] == 'redirect' || $this->vacation['action'] == 'copy';

        if (!empty($domains)) {
            sort($domains);

            $domain_select = new html_select(array('name' => 'action_domain', 'id' => 'action_domain'));
            $domain_select->add(array_combine($domains, $domains));

            if ($redirect && $this->vacation['target']) {
                $parts = explode('@', $this->vacation['target']);
                if (!empty($parts)) {
                    $this->vacation['domain'] = array_pop($parts);
                    $this->vacation['target'] = implode('@', $parts);
                }
            }
        }

        // redirect target
        $action_target = ' <span id="action_target_span" style="display:' . ($redirect ? 'inline' : 'none') . '">'
            . '<input type="text" name="action_target" id="action_target"'
            . ' value="' .($redirect ? rcube::Q($this->vacation['target'], 'strict', false) : '') . '"'
            . (!empty($domains) ? ' size="20"' : ' size="35"') . '/>'
            . (!empty($domains) ? ' @ ' . $domain_select->show($this->vacation['domain']) : '')
            . '</span>';

        // Message tab
        $table = new html_table(array('cols' => 2));

        $table->add('title', html::label('vacation_subject', $this->plugin->gettext('vacation.subject')));
        $table->add(null, $subject->show($this->vacation['subject']));
        $table->add('title', html::label('vacation_reason', $this->plugin->gettext('vacation.body')));
        $table->add(null, $reason->show($this->vacation['reason']));

        if ($date_extension || $regex_extension) {
            $table->add('title', html::label('vacation_datefrom', $this->plugin->gettext('vacation.dates')));
            $table->add(null,
                $this->plugin->gettext('vacation.from'). ' ' . $date_from->show($date_value['from'])
                . ' ' . $this->plugin->gettext('vacation.to'). ' ' . $date_to->show($date_value['to'])
            );
        }

        $table->add('title', html::label('vacation_status', $this->plugin->gettext('vacation.status')));
        $table->add(null, $status->show($this->vacation['disabled'] ? 'off' : 'on'));

        $out .= html::tag('fieldset', $class, html::tag('legend', null, $this->plugin->gettext('vacation.reply')) . $table->show($attrib));

        // Advanced tab
        $table = new html_table(array('cols' => 2));

        $table->add('title', html::label('vacation_addresses', $this->plugin->gettext('vacation.addresses')));
        $table->add(null, $addresses);
        $table->add('title', html::label('vacation_interval', $this->plugin->gettext('vacation.interval')));
        $table->add(null, $interval_txt);

        if ($after) {
            $table->add('title', html::label('vacation_after', $this->plugin->gettext('vacation.after')));
            $table->add(null, $after->show($this->vacation['idx'] - 1));
        }

        $table->add('title', html::label('vacation_action', $this->plugin->gettext('vacation.action')));
        $table->add('vacation', $action->show($this->vacation['action']) . $action_target);

        $out .= html::tag('fieldset', $class, html::tag('legend', null, $this->plugin->gettext('vacation.advanced')) . $table->show($attrib));

        $out .= '</form>';

        $this->rc->output->add_gui_object('sieveform', $form_id);

        return $out;
    }

    public static function build_regexp_tests($date_from, $date_to, &$error)
    {
        $tests    = array();
        $dt_from  = rcube_utils::anytodatetime($date_from);
        $dt_to    = rcube_utils::anytodatetime($date_to);
        $interval = $dt_from->diff($dt_to);

        if ($interval->invert || $interval->days > 365) {
            $error = 'managesieve.invaliddateformat';
            return;
        }

        $dt_i     = $dt_from;
        $interval = new DateInterval('P1D');
        $matchexp = '';

        while (!$dt_i->diff($dt_to)->invert) {
            $days     = (int) $dt_i->format('d');
            $matchexp .= $days < 10 ? "[ 0]$days" : $days;

            if ($days == $dt_i->format('t') || $dt_i->diff($dt_to)->days == 0) {
                $test = array(
                    'test' => 'header',
                    'type' => 'regex',
                    'arg1' => 'received',
                    'arg2' => '('.$matchexp.') '.$dt_i->format('M Y')
                );

                $tests[]  = $test;
                $matchexp = '';
            }
            else {
                $matchexp .= '|';
            }

            $dt_i->add($interval);
        }

        return $tests;
    }

    public static function parse_regexp_tests($tests)
    {
        $rx_from = '/^\(([0-9]{2}).*\)\s([A-Za-z]+)\s([0-9]{4})/';
        $rx_to   = '/^\(.*([0-9]{2})\)\s([A-Za-z]+)\s([0-9]{4})/';
        $result  = array();

        foreach ((array) $tests as $test) {
            if ($test['test'] == 'header' && $test['type'] == 'regex' && $test['arg1'] == 'received') {
                $textexp = preg_replace('/\[ ([^\]]*)\]/', '0', $test['arg2']);

                if (!$result['from'] && preg_match($rx_from, $textexp, $matches)) {
                    $result['from'] = $matches[1]." ".$matches[2]." ".$matches[3];
                }

                if (preg_match($rx_to, $textexp, $matches)) {
                    $result['to'] = $matches[1]." ".$matches[2]." ".$matches[3];
                }
            }
        }

        return $result;
    }
}