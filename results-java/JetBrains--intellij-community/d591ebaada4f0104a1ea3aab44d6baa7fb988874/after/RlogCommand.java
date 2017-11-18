package com.intellij.cvsSupport2.cvsoperations.cvsLog;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.netbeans.lib.cvsclient.IClientEnvironment;
import org.netbeans.lib.cvsclient.IRequestProcessor;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.AbstractCommand;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.LogMessageParser;
import org.netbeans.lib.cvsclient.event.ICvsListener;
import org.netbeans.lib.cvsclient.event.ICvsListenerRegistry;
import org.netbeans.lib.cvsclient.event.IEventSender;
import org.netbeans.lib.cvsclient.file.FileObject;
import org.netbeans.lib.cvsclient.progress.IProgressViewer;
import org.netbeans.lib.cvsclient.progress.sending.DummyRequestsProgressHandler;
import org.netbeans.lib.cvsclient.request.CommandRequest;
import org.netbeans.lib.cvsclient.request.Requests;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RlogCommand extends AbstractCommand {

  private String myModuleName = ".";

  private boolean myHeadersOnly = true;
  private boolean myNoTags = false;
  private Date myDateTo;
  private Date myDateFrom;
  private boolean mySuppressEmptyHeaders = true;
  @NonNls private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  // Implemented ============================================================

  public final boolean execute(IRequestProcessor requestProcessor,
                               IEventSender eventSender,
                               ICvsListenerRegistry listenerRegistry,
                               IClientEnvironment clientEnvironment,
                               IProgressViewer progressViewer) throws CommandException {
    final Requests requests = new Requests(CommandRequest.RLOG, clientEnvironment);
    requests.addArgumentRequest(myHeadersOnly, "-h");
    requests.addArgumentRequest(myNoTags, "-N");
    requests.addArgumentRequest(mySuppressEmptyHeaders, "-S");

    requests.addArgumentRequest(getDateFilter(), "-d");

    requests.addArgumentRequest(myModuleName);

    final ICvsListener parser = new LogMessageParser(eventSender, clientEnvironment.getCvsFileSystem());
    parser.registerListeners(listenerRegistry);
    try {
      return requestProcessor.processRequests(requests, new DummyRequestsProgressHandler());
    }
    finally {
      parser.unregisterListeners(listenerRegistry);
    }

  }

  @Nullable
  private String getDateFilter() {
    if (myDateFrom == null && myDateTo == null) {
      return null;
    }

    final StringBuffer result = new StringBuffer();

    if (myDateFrom == null) {
      result.append('<');
      result.append(DATE_FORMAT.format(myDateTo));
    }
    else if (myDateTo == null) {
      result.append('>');
      result.append(DATE_FORMAT.format(myDateFrom));
    }
    else {
      result.append(DATE_FORMAT.format(myDateFrom));
      result.append('<');
      result.append(DATE_FORMAT.format(myDateTo));
    }

    return result.toString();
  }


  public final String getCvsCommandLine() {
    //noinspection HardCodedStringLiteral
    final StringBuffer cvsCommandLine = new StringBuffer("log ");
    cvsCommandLine.append(getCVSArguments());
    appendFileArguments(cvsCommandLine);
    return cvsCommandLine.toString();
  }

  public final void resetCvsCommand() {
    super.resetCvsCommand();
    setRecursive(true);
  }

  public void setHeadersOnly(final boolean headersOnly) {
    myHeadersOnly = headersOnly;
  }

  public void setNoTags(final boolean noTags) {
    myNoTags = noTags;
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private String getCVSArguments() {
    final StringBuffer cvsArguments = new StringBuffer();
    //noinspection HardCodedStringLiteral
    if (myHeadersOnly) {
      cvsArguments.append("-h ");
    }

    if (myNoTags) {
      cvsArguments.append("-N ");
    }

    return cvsArguments.toString();
  }

  public void setModuleName(final String moduleName) {
    myModuleName = moduleName;
  }

  // Utils ==================================================================

  protected final void addModifiedRequest(FileObject fileObject, Entry entry, Requests requests, IClientEnvironment clientEnvironment) {
    requests.addIsModifiedRequest(fileObject);
  }

  public void setDateFrom(final Date dateFrom) {
    myDateFrom = dateFrom;
  }

  public void setDateTo(final Date dateTo) {
    myDateTo = dateTo;
  }

  public void setSuppressEmptyHeaders(final boolean suppressEmptyHeaders) {
    mySuppressEmptyHeaders = suppressEmptyHeaders;
  }
}