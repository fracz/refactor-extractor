package com.android.systemui.statusbar.policy;

import android.net.NetworkCapabilities;

import com.android.systemui.statusbar.policy.NetworkControllerImpl.IconState;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class NetworkControllerEthernetTest extends NetworkControllerBaseTest {

    public void testEthernetIcons() {
        verifyLastEthernetIcon(false, 0);

        setEthernetState(true, false);   // Connected, unvalidated.
        verifyLastEthernetIcon(true, EthernetIcons.ETHERNET_ICONS[0][0]);

        setEthernetState(true, true);    // Connected, validated.
        verifyLastEthernetIcon(true, EthernetIcons.ETHERNET_ICONS[1][0]);

        setEthernetState(true, false);   // Connected, unvalidated.
        verifyLastEthernetIcon(true, EthernetIcons.ETHERNET_ICONS[0][0]);

        setEthernetState(false, false);  // Disconnected.
        verifyLastEthernetIcon(false, 0);
    }

    protected void setEthernetState(boolean connected, boolean validated) {
        setConnectivity(validated ? 100 : 0, NetworkCapabilities.TRANSPORT_ETHERNET, connected);
    }

    protected void verifyLastEthernetIcon(boolean visible, int icon) {
        ArgumentCaptor<IconState> iconArg = ArgumentCaptor.forClass(IconState.class);

        Mockito.verify(mCallbackHandler, Mockito.atLeastOnce()).setEthernetIndicators(
                iconArg.capture());
        IconState iconState = iconArg.getValue();
        assertEquals("Ethernet visible, in status bar", visible, iconState.visible);
        assertEquals("Ethernet icon, in status bar", icon, iconState.icon);
    }
}