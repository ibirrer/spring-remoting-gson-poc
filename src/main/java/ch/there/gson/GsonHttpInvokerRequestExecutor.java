package ch.there.gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;

import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import ch.there.gson.Server.User;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

public class GsonHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {

  private final int remoteApiVersion;

  public GsonHttpInvokerRequestExecutor(int remoteApiVersion) {
    this.remoteApiVersion = remoteApiVersion;
  }

  public static void main(String[] args) {
    String expectedJson = "{\"exceptionClass\":\"java.io.IOException\",\"detailMessage\":\"io exception\"}";
    Gson gson = new Gson();
    String json = gson.toJson(new IOException("io exception"));
  }

  @Override
  protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException {
    Gson gson = GsonFactory.getGson();
    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(os));
    try {
      gson.toJson(invocation, RemoteInvocation.class, jsonWriter);
    } finally {
      jsonWriter.close();
    }
  }

  @Override
  protected void prepareConnection(HttpURLConnection connection, int contentLength) throws IOException {
    super.prepareConnection(connection, contentLength);
    connection.setRequestProperty("rpc-version", Integer.toString(remoteApiVersion));
  }

  @Override
  protected RemoteInvocationResult readRemoteInvocationResult(InputStream is, String codebaseUrl) throws IOException,
      ClassNotFoundException {
    try {
      JsonParser parser = new JsonParser();
      Reader reader = new BufferedReader(new InputStreamReader(is));
      JsonObject removeInvocationResult = parser.parse(reader).getAsJsonObject();

      // FIXME: this returnType needs to be determined dynamically from the
      // service method
      Type returnType = new TypeToken<List<User>>() {}.getType();

      JsonElement value = removeInvocationResult.get("value");
      return new RemoteInvocationResult(GsonFactory.getGson().fromJson(value, returnType));

      // FIXME: handle expection (removeInvocationResult.get("exception"))

    } finally {
      is.close();
    }
  }
}
