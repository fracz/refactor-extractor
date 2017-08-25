<?php

class AccountController extends BaseController {

    protected $user;

    /**
     * Load the injected models.
     * Setup access permissions.
     */
    public function __construct(Page $page, User $user) {
        $this->page = $page;
        $this->user = $user;

        $this->setPermissions(array(
            'getProfile'    => 'user',
            'deleteProfile' => 'user',
            'patchDetails'  => 'user',
            'patchPassword' => 'user',
            'getLogout'     => 'user',
        ));

        parent::__construct();
    }

    /**
     * Redirect to the profile page.
     *
     * @return Response
     */
    public function getIndex() {
        Session::flash('', ''); // work around laravel bug
        Session::reflash();
        Log::info('Redirecting from account to the profile page');
        return Redirect::route('account.profile');
    }

    /**
     * Display the login page.
     *
     * @return Response
     */
    public function getLogin() {
        return $this->viewMake('account.login');
    }

    /**
     * Try to log the user in.
     *
     * @return Response
     */
    public function postLogin() {
        $remember = Binput::get('rememberMe');

        $input = array(
            'email' => Binput::get('email'),
            'password' => Binput::get('password'),
        );

        $rules = array(
            'email' => 'required|min:4|max:32|email',
            'password' => 'required|min:6',
        );

        $v = Validator::make($input, $rules);
        if ($v->fails()) {
            Log::info('Login failed because validation failed', array('Email' => $input['email'], 'Messages' => $v->messages()->all()));
            return Redirect::route('account.login')->withErrors($v)->withInput();
        }

        try {
            $throttle = Sentry::getThrottleProvider()->findByUserId(Sentry::getUserProvider()->findByLogin($input['email'])->id);
            $throttle->check();

            $user = Sentry::authenticate($input, $remember);
        } catch (Cartalyst\Sentry\Users\UserNotFoundException $e) {
            Log::notice($e);
            Session::flash('error', 'That user does not exist.');
            return Redirect::route('account.login')->withErrors($v)->withInput();
        } catch (Cartalyst\Sentry\Users\UserNotActivatedException $e) {
            Log::notice($e);
            Session::flash('error', 'You have not yet activated this account.');
            return Redirect::route('account.login')->withErrors($v)->withInput();
        } catch (Cartalyst\Sentry\Users\WrongPasswordException $e) {
            Log::notice($e);
            Session::flash('error', 'Your password was incorrect.');
            return Redirect::route('account.login')->withErrors($v)->withInput();
        } catch (Cartalyst\Sentry\Throttling\UserSuspendedException $e) {
            Log::notice($e);
            $time = $throttle->getSuspensionTime();
            Session::flash('error', "Your account has been suspended for $time minutes.");
            return Redirect::route('account.login')->withErrors($v)->withInput();
        } catch (Cartalyst\Sentry\Throttling\UserBannedException $e) {
            Log::notice($e);
            Session::flash('error', 'You have been banned. Please contact support.');
            return Redirect::route('account.login')->withErrors($v)->withInput();
        }

        Log::info('Login successful', array('Email' => $input['email']));
        return Redirect::intended(URL::route('pages.show', array('pages' => 'home')));
    }

    /**
     * Display the registration page.
     *
     * @return Response
     */
    public function getRegister() {
        if (!Config::get('cms.regallowed')) {
            Log::notice('Registration disabled');
            Session::flash('error', 'Registration is currently disabled.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        }

        return $this->viewMake('account.register');
    }

    /**
     * Try to register the user.
     *
     * @return Response
     */
    public function postRegister() {
        if (!Config::get('cms.regallowed')) {
            Log::notice('Registration disabled');
            Session::flash('error', 'Registration is currently disabled.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        }

        $input = array(
            'first_name' => Binput::get('first_name'),
            'last_name' => Binput::get('last_name'),
            'email' => Binput::get('email'),
            'password' => Binput::get('password'),
            'password_confirmation' => Binput::get('password_confirmation'),
        );

        $rules = array (
            'first_name' => 'required|min:2|max:32',
            'last_name' => 'required|min:2|max:32',
            'email' => 'required|min:4|max:32|email',
            'password' => 'required|min:6|confirmed',
            'password_confirmation' => 'required',
        );

        $v = Validator::make($input, $rules);
        if ($v->fails()) {
            Log::info('Registration failed because validation failed', array('Email' => $input['email'], 'Messages' => $v->messages()->all()));
            return Redirect::route('account.register')->withErrors($v)->withInput();
        }

        try {
            unset($input['password_confirmation']);

            $user = Sentry::register($input);

            if (!Config::get('cms.regemail')) {
                $user->attemptActivation($user->GetActivationCode());
                $user->addGroup(Sentry::getGroupProvider()->findByName('Users'));

                Log::info('Registration successful, activation not required', array('Email' => $input['email']));
                Session::flash('success', 'Your account has been created successfully.');
                return Redirect::route('pages.show', array('pages' => 'home'));
            }

            $data = array(
                'view' => 'emails.welcome',
                'link' => URL::route('account.activate', array('id' => $user->getId(), 'code' => $user->GetActivationCode())),
                'email' => $user->getLogin(),
                'subject' => Config::get('cms.name').' - Welcome',
            );

            try {
                Queue::push('MailHandler', $data);
            } catch (Exception $e) {
                Log::alert($e);
                $user->delete();
                Session::flash('error', 'We were unable to create your account. Please contact support.');
                return Redirect::route('account.register');
            }

            Log::info('Registration successful, activation required', array('Email' => $input['email']));
            Session::flash('success', 'Your account has been created. Check your email for the confirmation link.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        } catch (Cartalyst\Sentry\Users\UserExistsException $e) {
            Log::notice($e);
            Session::flash('error', 'User already exists.');
            return Redirect::route('account.register')->withErrors($v)->withInput();
        }
    }

    /**
     * Display the user's profile.
     *
     * @return Response
     */
    public function getProfile() {
        return $this->viewMake('account.profile');
    }

    /**
     * Delete the user's profile.
     *
     * @return Response
     */
    public function deleteProfile() {
        $user = Sentry::getUser();
        $this->checkUser($user);

        Sentry::logout();

        $user->delete();

        Session::flash('success', 'Your account has been deleted successfully.');
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    /**
     * Update the user's details.
     *
     * @return Response
     */
    public function patchDetails() {
        $input = array(
            'first_name' => Binput::get('first_name'),
            'last_name' => Binput::get('last_name'),
            'email' => Binput::get('email'),
        );

        $rules = array (
            'first_name' => 'required|min:2|max:32',
            'last_name' => 'required|min:2|max:32',
            'email' => 'required|min:4|max:32|email',
        );

        $v = Validator::make($input, $rules);

        $v = Validator::make($input, $rules);
        if ($v->fails()) {
            return Redirect::route('account.profile')->withInput()->withErrors($v->errors());
        }

        $user = Sentry::getUser();
        $this->checkUser($user);

        $user->update($input);

        Session::flash('success', 'Your details have been updated successfully.');
        return Redirect::route('account.profile');
    }

    /**
     * Update the user's password.
     *
     * @return Response
     */
    public function patchPassword() {
        $input = array(
            'password' => Binput::get('password'),
            'password_confirmation' => Binput::get('password_confirmation'),
        );

        $rules = array (
            'password' => 'required|min:6|confirmed',
            'password_confirmation' => 'required',
        );

        $v = Validator::make($input, $rules);
        if ($v->fails()) {
            return Redirect::route('account.profile')->withInput()->withErrors($v->errors());
        }

        unset($input['password_confirmation']);

        $user = Sentry::getUser();
        $this->checkUser($user);

        $user->update($input);

        Session::flash('success', 'Your password has been updated successfully.');
        return Redirect::route('account.profile');
    }

    /**
     * Display the password reset page.
     *
     * @return Response
     */
    public function getReset() {
        return $this->viewMake('account.reset');
    }

    /**
     * Queue the sending of the password reset email.
     *
     * @return Response
     */
    public function postReset() {
        $input = array(
            'email' => Binput::get('email'),
            );

        $rules = array (
            'email' => 'required|min:4|max:32|email',
            );

        $v = Validator::make($input, $rules);
        if ($v->fails()) {
            return Redirect::route('account.reset')->withErrors($v)->withInput();
        }

        try {
            $user = Sentry::getUserProvider()->findByLogin($input['email']);

            $data = array(
                'view' => 'emails.reset',
                'link' => URL::route('account.password', array('id' => $user->getId(), 'code' => $user->getResetPasswordCode())),
                'email' => $user->getLogin(),
                'subject' => Config::get('cms.name').' - Password Reset Confirmation',
            );

            try {
                Queue::push('MailHandler', $data);
            } catch (Exception $e) {
                Log::alert($e);
                Session::flash('error', 'We were unable to reset your password. Please contact support.');
                return Redirect::route('account.reset');
            }

            Log::info('Reset email sent', array('Email' => $input['email']));
            Session::flash('success', 'Check your email for password reset information.');
            return Redirect::route('account.reset');
        } catch (Cartalyst\Sentry\Users\UserNotFoundException $e) {
            Log::notice($e);
            Session::flash('error', 'That user does not exist.');
            return Redirect::route('account.reset');
        }
    }

    /**
     * Reset the user's passowrd.
     *
     * @return Response
     */
    public function getPassword($id = null, $code = null) {
        if ($id === null || $code === null) {
            App::abort(400);
        }

        try {
            $user = Sentry::getUserProvider()->findById($id);

            $password = $this->_generatePassword(12,8);

            if (!$user->attemptResetPassword($code, $password)) {
                Log::error('There was a problem resetting a password', array('Id' => $id));
                Session::flash('error', 'There was a problem resetting your password. Please contact support.');
                return Redirect::route('base');
            }

            $data = array(
                'view' => 'emails.password',
                'password' => $password,
                'email' => $user->getLogin(),
                'subject' => Config::get('cms.name').' - New Password Information',
            );

            Log::info('Password reset successfully', array('Email' => $input['email']));
            Session::flash('success', 'Your password has been changed. Check your email for the new password.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        } catch (Cartalyst\Sentry\Users\UserNotFoundException $e) {
            Log::error($e);
            Session::flash('error', 'There was a problem resetting your password. Please contact support.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        }
    }

    /**
     * Activate the user.
     *
     * @return Response
     */
    public function getActivate($id = null, $code = null) {
        if ($id === null || $code === null) {
            App::abort(400);
        }

        try {
            $user = Sentry::getUserProvider()->findById($id);

            if (!$user->attemptActivation($code)) {
                Session::flash('error', 'There was a problem activating this account. Please contact support.');
                return Redirect::route('pages.show', array('pages' => 'home'));
            }

            $user->addGroup(Sentry::getGroupProvider()->findByName('Users'));

            Log::info('Activation successful', array('Email' => $user->email));
            Session::flash('success', 'Your account has been activated successfully. You may now login.');
            return Redirect::route('account.login', array('pages' => 'home'));
        } catch (Cartalyst\Sentry\Users\UserNotFoundException $e) {
            Log::error($e);
            Session::flash('error', 'There was a problem activating this account. Please contact support.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        } catch (Cartalyst\SEntry\Users\UserAlreadyActivatedException $e) {
            Log::notice($e);
            Session::flash('warning', 'You have already activated this account. You may want to login.');
            return Redirect::route('account.login', array('pages' => 'home'));
        }
    }

    /**
     * Log the user out.
     *
     * @return Response
     */
    public function getLogout() {
        Log::info('User logged out', array('Email' => Sentry::getUser()->email));
        Sentry::logout();
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    protected function checkUser($user) {
        if (!$user) {
            return App::abort(404, 'User Not Found');
        }
    }

    /**
     * Generate a random password.
     *
     * @return String
     */
    protected function _generatePassword($length=9, $strength=4) {
        $vowels = 'aeiouy';
        $consonants = 'bcdfghjklmnpqrstvwxz';
        if ($strength & 1) {
            $consonants .= 'BCDFGHJKLMNPQRSTVWXZ';
        }
        if ($strength & 2) {
            $vowels .= "AEIOUY";
        }
        if ($strength & 4) {
            $consonants .= '23456789';
        }
        if ($strength & 8) {
            $consonants .= '@#$%';
        }

        $password = '';
        $alt = time() % 2;
        for ($i = 0; $i < $length; $i++) {
            if ($alt == 1) {
                $password .= $consonants[(rand() % strlen($consonants))];
                $alt = 0;
            } else {
                $password .= $vowels[(rand() % strlen($vowels))];
                $alt = 1;
            }
        }

        return $password;
    }
}