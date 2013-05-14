package ch.there.gson;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ch.there.gson.Server.UserService;
import ch.there.gson.Server.UserType;

public class Client {

  @Configuration
  static class Config {

    @Bean
    public HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBeanV1() {
      HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
      factory.setHttpInvokerRequestExecutor(new GsonHttpInvokerRequestExecutor(1));
      factory.setServiceUrl("http://localhost:8080/users");
      factory.setServiceInterface(UserService.class);
      return factory;
    }

    @Bean
    public HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBeanV2() {
      HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
      factory.setHttpInvokerRequestExecutor(new GsonHttpInvokerRequestExecutor(2));
      factory.setServiceUrl("http://localhost:8080/users");
      factory.setServiceInterface(UserService.class);
      return factory;
    }

    @Bean
    public UserService userServiceV1() {
      return (UserService) httpInvokerProxyFactoryBeanV1().getObject();
    }

    @Bean
    public UserService userServiceV2() {
      return (UserService) httpInvokerProxyFactoryBeanV2().getObject();
    }
  }

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
    ctx.start();
    UserService userServiceV1 = (UserService) ctx.getBean("userServiceV1");
    System.out.println("GOOD: " + userServiceV1.getUsers(UserType.GOOD));
    System.out.println("BAD: " + userServiceV1.getUsers(UserType.BAD));

    UserService userServiceV2 = (UserService) ctx.getBean("userServiceV2");
    System.out.println("GOOD: " + userServiceV2.getUsers(UserType.GOOD));
    System.out.println("BAD: " + userServiceV2.getUsers(UserType.BAD));
  }
}
