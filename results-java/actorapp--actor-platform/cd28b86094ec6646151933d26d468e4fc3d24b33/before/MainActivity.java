package im.actor.messenger.app.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import net.hockeyapp.android.UpdateManager;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.activity.base.ControllerActivity;
import im.actor.messenger.app.activity.controllers.MainBaseController;
import im.actor.messenger.app.activity.controllers.MainPhoneController;
import im.actor.messenger.app.fragment.tour.TourActivity;
import im.actor.messenger.app.Core;
import im.actor.model.AuthState;
import im.actor.model.entity.Dialog;

public class MainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        return new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);


        checkForUpdates();

        if (Core.messenger().getAuthState() != AuthState.LOGGED_IN) {
            startActivity(new Intent(this, TourActivity.class));
            finish();
            return;
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_main)));
    }

    public void onDialogClicked(Dialog item) {
        getController().onItemClicked(item);
    }

    private void checkForUpdates() {
        if (BuildConfig.HOCKEY != null) {
            UpdateManager.register(this, BuildConfig.HOCKEY);
        }
    }
}