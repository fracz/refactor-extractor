package org.jetbrains.plugins.ipnb.format;

import org.jetbrains.plugins.ipnb.format.cells.IpnbCell;

import java.util.List;

public class IpnbFile {
  private final IpnbParser.IpnbFileRaw myRawFile;
  private final List<IpnbCell> myCells;
  private final String myPath;

  IpnbFile(IpnbParser.IpnbFileRaw rawFile, List<IpnbCell> cells, String path) {
    myRawFile = rawFile;
    myCells = cells;
    myPath = path;
  }

  public List<IpnbCell> getCells() {
    return myCells;
  }

  public String getPath() {
    return myPath;
  }

  public IpnbParser.IpnbFileRaw getRawFile() {
    return myRawFile;
  }
}