import com.intellij.openapi.util.Ref;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ipnb.protocol.IpnbConnection;
import org.jetbrains.plugins.ipnb.protocol.IpnbConnectionListenerBase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 *
 * * Message Spec
 *   http://ipython.org/ipython-doc/dev/development/messaging.html
 *
 * * Notebook REST API
 *   https://github.com/ipython/ipython/wiki/IPEP-16%3A-Notebook-multi-directory-dashboard-and-URL-mapping
 *
 * @author vlan
 */
public class WebSocketConnectionTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    //WebSocketImpl.DEBUG = true;
  }

  public void testStartAndShutdownKernel() throws URISyntaxException, IOException, InterruptedException {
    final IpnbConnection connection = new IpnbConnection(getTestServerURI(), new IpnbConnectionListenerBase() {
      @Override
      public void onOpen(@NotNull IpnbConnection connection) {
        assertTrue(connection.getKernelId().length() > 0);
        connection.shutdown();
      }
    });
    connection.close();
  }

  public void testBasicWebSocket() throws IOException, URISyntaxException, InterruptedException {
    final Ref<Boolean> evaluated = Ref.create(false);
    final IpnbConnection connection = new IpnbConnection(getTestServerURI(), new IpnbConnectionListenerBase() {
      private String myMessageId;

      @Override
      public void onOpen(@NotNull IpnbConnection connection) {
        myMessageId = connection.execute("2 + 2");
      }

      @Override
      public void onOutput(@NotNull IpnbConnection connection, @NotNull String parentMessageId, @NotNull Map<String, String> outputs) {
        if (myMessageId.equals(parentMessageId)) {
          assertEquals("4", outputs.get("text/plain"));
          evaluated.set(true);
          connection.shutdown();
        }
      }
    });
    connection.close();
    assertTrue(evaluated.get());
  }

  @NotNull
  public static URI getTestServerURI() {
    try {
      return new URI("http://127.0.0.1:8888");
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}