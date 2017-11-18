package org.jetbrains.plugins.ipnb.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ipnb.format.cells.output.CellOutput;

import java.util.List;

/**
 * @author vlan
 */
public class IpnbConnectionListenerBase implements IpnbConnectionListener {
  @Override
  public void onOpen(@NotNull IpnbConnection connection) {
  }

  @Override
  public void onOutput(@NotNull IpnbConnection connection, @NotNull String parentMessageId, @NotNull List<CellOutput> outputs) {
  }
}