package ch.there.gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.support.SimpleHttpServerFactoryBean;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {

  static class User {
    private final String firstName;
    private final String lastName;

    public User(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    @Override
    public String toString() {
      return firstName + " " + lastName;
    }
  }

  public static enum UserType {
    GOOD, BAD;
  }

  interface UserService {
    List<User> getUsers(UserType userType);
  }

  private static final class UserServiceImpl implements UserService {
    @Override
    public List<User> getUsers(UserType userType) {
      if (CallerRemoteApiVersion.getVersion() == 1) {
        switch (userType) {
        case GOOD:
          return Arrays.asList(new User("Anakin", "Skywalker"), new User("Obi-Wan", "Kenobi"));
        case BAD:
          return Arrays.asList(new User("Darth", "Maul"));
        }
      }

      if (CallerRemoteApiVersion.getVersion() == 2) {
        switch (userType) {
        case GOOD:
          return Arrays.asList(new User("Luke", "Skywalker"), new User("Obi-Wan", "Kenobi"));
        case BAD:
          return Arrays.asList(new User("Anakin", "Skywalker"));
        }
      }

      return Collections.emptyList();
    }
  }

  @Configuration
  static class Config {

    @Bean
    public UserService userService() {
      return new UserServiceImpl();
    }

    @Bean
    public HttpHandler userServiceExporter() {
      GsonInvokerServiceExporter exporter = new GsonInvokerServiceExporter();
      exporter.setService(userService());
      exporter.setServiceInterface(UserService.class);
      return exporter;
    }

    @Bean
    public SimpleHttpServerFactoryBean simpleHttpServerFactoryBean() {
      SimpleHttpServerFactoryBean factory = new SimpleHttpServerFactoryBean();
      factory.setPort(8080);
      HashMap<String, HttpHandler> contexts = new HashMap<>();
      contexts.put("/users", userServiceExporter());
      factory.setContexts(contexts);
      return factory;
    }

    public HttpServer httpServer() {
      return simpleHttpServerFactoryBean().getObject();
    }
  }

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
    ctx.start();
  }
}
