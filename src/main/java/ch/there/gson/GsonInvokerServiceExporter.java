/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.there.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Collection;

import org.springframework.remoting.rmi.RemoteInvocationSerializingExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GsonInvokerServiceExporter extends RemoteInvocationSerializingExporter
    implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    Gson gson = GsonFactory.getGson();

    try {
      Integer rpcVersion = Integer.valueOf(exchange.getRequestHeaders().getFirst("rpc-version"));
      CallerRemoteApiVersion.setVersion(rpcVersion);
      InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
      JsonParser parser = new JsonParser();
      JsonObject remote = parser.parse(reader).getAsJsonObject();
      String methodName = remote.get("methodName").getAsString();

      Type collectionType = new TypeToken<Collection<Class<?>>>() {}.getType();
      Collection<Class<?>> parameterTypes = gson.fromJson(remote.get("parameterTypes"), collectionType);

      JsonArray args = remote.get("arguments").getAsJsonArray();
      Class<?>[] params = parameterTypes.toArray(new Class[parameterTypes.size()]);

      Object[] arguments = new Object[params.length];

      for (int i = 0; i < params.length; i++) {
        Class<?> clazz = params[i];
        Object argument = gson.fromJson(args.get(i), clazz);
        arguments[i] = argument;
      }

      RemoteInvocation remoteInvocation = new RemoteInvocation(methodName, params, arguments);
      RemoteInvocationResult result = invokeAndCreateResult(remoteInvocation, getProxy());
      writeRemoteInvocationResult(exchange, result);
      exchange.close();
    } catch (Throwable e) {
      e.printStackTrace();
    }

  }

  protected RemoteInvocation readRemoteInvocation(HttpExchange exchange)
      throws IOException, ClassNotFoundException {
    return readRemoteInvocation(exchange, exchange.getRequestBody());
  }

  protected RemoteInvocation readRemoteInvocation(HttpExchange exchange, InputStream is)
      throws IOException, ClassNotFoundException {

    ObjectInputStream ois = createObjectInputStream(decorateInputStream(exchange, is));
    return doReadRemoteInvocation(ois);
  }

  protected InputStream decorateInputStream(HttpExchange exchange, InputStream is) throws IOException {
    return is;
  }

  protected void writeRemoteInvocationResult(HttpExchange exchange, RemoteInvocationResult result)
      throws IOException {

    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, 0);
    writeRemoteInvocationResult(exchange, result, exchange.getResponseBody());
  }

  protected void writeRemoteInvocationResult(
      HttpExchange exchange, RemoteInvocationResult result, OutputStream os) throws IOException {
    Gson gson = GsonFactory.getGson();
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
    gson.toJson(result, RemoteInvocationResult.class, writer);
    writer.flush();
  }

  protected OutputStream decorateOutputStream(HttpExchange exchange, OutputStream os) throws IOException {
    return os;
  }

}
