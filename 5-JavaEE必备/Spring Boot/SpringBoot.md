# Spring Boot笔记

## 1.SpringBoot入门

#### 1.1 Spring Boot简介

#### 1.2 Spring Boot工程的创建

#### 1.3 容器功能

##### 1.3.1 添加组件

1. @Configuration

   表示当前类为一个 配置类，等同于spring的bean.xml文件

   例如：将两个实体类Pet、User注入到spring容器中

   - spring做法：在resources下创建一个xml文件，文件内容如下

     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
     
         <bean id="user" class="com.kyn.pojo.User">
             <property name="name" value="zhangsan"></property>
             <property name="age" value="18"></property>
         </bean>
     
         <bean id="pet" class="com.kyn.pojo.Pet">
             <property name="name" value="小黄"></property>
             <property name="color" value="blue"></property>
          </bean>
     
     </beans>
     ```

   - springBoot做法：无需配置文件，直接创建一个类，在该类上使用@Configuration注解，该注解表示该类是一个配置类。在配置类中通过方法+@Bean注解的方式将组件添加到spring容器中。

     ```java
     package com.kyn.conf;
     
     import com.kyn.pojo.Pet;
     import com.kyn.pojo.User;
     import org.springframework.context.annotation.Bean;
     import org.springframework.context.annotation.Configuration;
     
     @Configuration  //等同于bean的xml配置文件
     public class MyConfig {
     
         @Bean  //等同于配置文件中的bean标签，方法名作为组件的id，返回的值就是组件在容器中的实例
         public User user(){
             return new User("zhangsan",18);
         }
     
         @Bean
         public Pet pet(){
             return new Pet("小黄","blue");
         }
     }
     ```

     > 说明：
     >
     > 1. spring容器中的组件默认是单实例的
     > 2. @Configuration标注的配置类本身也是组件       
     > 3. spring boot5.2之后，Configuration注解中添加了属性proxyBeanMethods

   - proxyBeanMethods属性

     用来指定@Bean注解标注的方法是否使用代理；

     proxyBeanMethods属性默认值是true，也就是说该配置类会被代理（CGLIB），**在同一个配置文件中调用其它被@Bean注解标注的方法获取对象时首先会检查spring容器中是否又该组件，有则直接从IOC容器之中获取，无论调用多少次，都是同一个对象；**

     如果设置为false,也就是不使用注解，**无需检查spring容器，每次调用@Bean标注的方法获取到的对象和IOC容器中的都不一样，是一个新的对象**，所以我们可以将此属性设置为false来提高性能；

   - 底层配置模式：**Full模式与Lite模式**

     - Full（proxyBeanMethods=true）：配置类组件之间有依赖关系，方法会被调用得到之前单实例组件，用Full模式
     - Lite（proxyBeanMethods=false）：配置 类组件之间无依赖关系用Lite模式加速容器启动过程，减少判断

     

2. @Bean、@Component、@Controller、@Service、@Repository

   

3. @ComponentScan、@Import

   - @Import：向容器中导入组件，该注解只能标注在类上并此类必须在spring 容器中。

   ​                           默认组件的名字是全类名

4. @Conditional

   条件装配，是Spring4新提供的注解，它的作用是按照一定的条件进行判断，满足条件才给容器注册bean。

   可以标注在类上或者方法中
   
   该注解包含如下派生注解，（使用时使用派生注解）
   
   ![image-20210109175523092](SpringBoot.assets/image-20210109175523092.png)

![image-20210109175834140](SpringBoot.assets/image-20210109175834140.png)

##### 1.3.2 原生配置文件引入

1. @ImportResource

   导入beans.xml配置文件，并将组件注入到容器中

##### 1.3.3 配置绑定

如何使用Java读取到properties或者yml文件中的内容，并且把它封装到JavaBean中，以供随时使用；

1. spring做法：

   ```java
   public class getProperties {
        public static void main(String[] args) throws FileNotFoundException, IOException      {
            Properties pps = new Properties();
            pps.load(new FileInputStream("a.properties"));
            Enumeration enum1 = pps.propertyNames();//得到配置文件的名字
            while(enum1.hasMoreElements()) {
                String strKey = (String) enum1.nextElement();
                String strValue = pps.getProperty(strKey);
                System.out.println(strKey + "=" + strValue);
                //封装到JavaBean。
            }
        }
    }
   ```

2. springboot做法：

   - 方法一：在需绑定参数的类上加@ConfigurationProperties

     - 在编写项目代码时，我们要求更灵活的配置，更好的模块化整合。在 Spring Boot 项目中，为满足以上要求，我们将大量的参数配置在 application.properties 或 application.yml 文件中，通过 `@ConfigurationProperties` 注解，我们可以方便的获取这些参数值

     - **只有容器中有的方法才能使用该注解**

     - 需指定一个前缀prefix

       ```java
       @ConfigurationProperties(prefix = "pet")
       public class Pet {
           private String name;
           private String color;
           
           .........
       }
       ```

       ```yaml
       #yml配置文件
       pet:
         name: aa
         color: blue
       ```

     - 可以用在类上也可以用在方法上

     - 类的字段必须有公共 setter 方法
     - 根据 Spring Boot 宽松的绑定规则，**类的属性名称必须与外部属性的名称匹配**
     - 前缀定义了哪些外部属性将绑定到类的字段上

   - 方法二：在配置类上使用@EnableConfigurationProperties + 在需绑定参数的类上使用@ConfigurationProperties

     - @EnableConfigurationProperties：开启属性配置功能

     - `@EnableConfigurationProperties("XXX.class")`：表示开启XXX类的属性配置，也表示把XXX自动注册到spring容器中
     - 实体类无需注册到spring容器中

#### 1.4 自动配置原理

##### 1.4.1 引导加载自动配置类

打开启动类的@SpringBootApplication 注解源码。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
    ..............
}
```

1. 前四个是专门（即只能）用于对注解进行注解的，称为元注解

2. @SpringBootConfiguration

   查看该注解的源码注解可知，该注解与@Configuration 注解功能相同，仅表示当前类为一个 JavaConfig 类，其就是为 Spring Boot 专门创建的一个注解。

   ```java
   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   @Configuration
   public @interface SpringBootConfiguration {
       @AliasFor(
           annotation = Configuration.class
       )
       boolean proxyBeanMethods() default true;
   }
   ```

3. @ComponentScan

   用于完成组件扫描。不过需要注意，其仅仅是指定了要扫描的包，并没有装配其中的类，这个真正装配这些类是@EnableAutoConfiguration 完成的

4. @EnableAutoConfiguration

   

##### 1.4.2 按需开启自动配置项



##### 1.4.3 修改默认配置



## 2.SpringBoot基础配置



## 3.模板引擎—Thymeleaf



## 4.Spring Boot整合Web开发

## 5.Spring Boot整合持久层技术

#### 5.1 整合JdbcTemplate

JdbcTemplate是Spring提供的一套JDBC模板框架，利用AOP技术来解决直接使用JDBC时大量重复代码的问题。JdbcTemplate虽然没有MyBatis那么灵活，但是比直接使用JDBC要方便很多。**Spring Boot中对JdbcTemplate的使用提供了自动化配置类JdbcTemplateAutoConfiguration**

##### 5.1.1 数据源的自动配置

1. 导入JDBC场景

   ```java
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
   </dependency>
   ```

   导入JDBC后，一些有关的依赖自动导入：

   ![image-20210110112126532](SpringBoot.assets/image-20210110112126532.png)

   **分析自动配置类**

   ![image-20210110115046670](SpringBoot.assets/image-20210110115046670.png)

   

   - DataSourceAutoConfiguration：数据源的自动配置

     ![image-20210110114931854](SpringBoot.assets/image-20210110114931854.png)

     

     - 打开DataSourceProperties

     ![image-20210110115258290](SpringBoot.assets/image-20210110115258290.png)

     > 修改数据源相关的配置，可以通过**spring.datasource**前缀指定属性修改

     - **数据库连接池的配置，是自己容器中没有DataSource才自动配置的**

       ![image-20210110115901829](SpringBoot.assets/image-20210110115901829.png)

     - 底层配置好的连接池是：**HikariDataSource**

   - DataSourceTransactionManagerAutoConfiguration： 事务管理器的自动配置

   - JdbcTemplateAutoConfiguration： **JdbcTemplate的自动配置，可以来对数据库进行crud**

     ![image-20210110123034270](SpringBoot.assets/image-20210110123034270.png)

   - > 当classpath下存在DataSource和JdbcTemplate并且DataSource只有一个实例时，自动配置才会生效

   - 

   - 打开JdbcProperties。

   - ![image-20210110123340603](SpringBoot.assets/image-20210110123340603.png)

   - > 可以修改这个配置项@ConfigurationProperties(prefix = **"spring.jdbc"**) 来修改JdbcTemplate

   - 

   - - @Bean@Primary   JdbcTemplate；容器中有这个组件

       ![image-20210110123515395](SpringBoot.assets/image-20210110123515395.png)

       > 若开发者没有提供JdbcOperations，则Spring Boot会自动向容器中注入一个JdbcTemplate（JdbcTemplate是JdbcOperations的子类）

   由此可以看到，开发者想要使用JdbcTemplate，只需要提供JdbcTemplate的依赖和DataSource依赖即可

   

   - JndiDataSourceAutoConfiguration： jndi的自动配置
   - XADataSourceAutoConfiguration： 分布式事务相关的

2. 导入数据库驱动依赖

   在jdbc依赖中我们发现官方并没有为我们导入数据库驱动，为什么导入JDBC场景，官方不导入驱动？因为官方不知道我们接下要操作什么数据库。

   ```java
   默认版本：<mysql.version>8.0.22</mysql.version>
   
           <dependency>
               <groupId>mysql</groupId>
               <artifactId>mysql-connector-java</artifactId>
               <!--<version>5.1.49</version>-->
           </dependency>
   想要修改版本
   1、直接依赖引入具体版本（maven的就近依赖原则）
   2、重新声明版本（maven的属性的就近优先原则）
       <properties>
           <java.version>1.8</java.version>
           <mysql.version>5.1.49</mysql.version>
       </properties>
   ```

3. 添加数据库配置项

   ```java
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/test?characterEncoding=utf-8&useSSL=false
       username: root
       password: 123456
       driver-class-name: com.mysql.jdbc.Driver
   #    type: com.zaxxer.hikari.HikariDataSource
   ```

   

4. 创建实体类

   ```java
   package com.kyn.pojo;
   
   public class User {
       private String name;
       private int age;
       
       //省略getter/setter
   }
   ```

5.  创建数据库访问层

   ```java
   @Repository
   public class UserDao {
   
       @Autowired
       JdbcTemplate jdbcTemplate;
   
       public int addUser(User user){
           return jdbcTemplate.update("insert into userinfo(name,age) values(?,?) ",
                                     user.getName(),user.getAge());
       }
   
       public User getUserByName(String name){
           return jdbcTemplate.queryForObject("select * from userinfo where name=?",
                                              new BeanPropertyRowMapper<>(User.class),name);
       }
   
       public List<User> getAllBooks(){
           return jdbcTemplate.query("select * from userinfo"
                                           ,new BeanPropertyRowMapper(User.class));
       }
   
   }
   ```

   > 代码解释：
   >
   > - 创建BookDao，注入JdbcTemplate。由于已经添加了spring-jdbc相关的依赖，JdbcTemplate会被自动注册到Spring容器中，因此这里可以直接注入JdbcTemplate使用。
   >
   > - 在JdbcTemplate中，增删改三种类型的操作主要使用update和batchUpdate方法来完成。query和queryForObject方法主要用来完成查询功能。另外，还有execute方法可以用来执行任意的SQL、call方法用来调用存储过程等。
   >
   > - 在执行查询操作时，需要有一个RowMapper将查询出来的列和实体类中的属性一一对应起来。如果列名和属性名都是相同的，那么可以直接使用BeanPropertyRowMapper；如果列名和属性名不同，就需要开发者自己实现RowMapper接口，将列和实体类属性一一对应起来。

6. 创建Service和Controller

   ```java
   @Service
   public class UserService {
   
       @Autowired
       UserDao userDao;
   
       public int addUser(User user){
           return userDao.addUser(user);
       }
   
       public User getUserByName(String name){
           return userDao.getUserByName(name);
       }
   
       public List<User> getAllUser(){
           return userDao.getAllBooks();
       }
   
   }
   
   
   ```

   ```java
   @RestController
   public class UserController {
   
       @Autowired
       UserService userService;
   
       @RequestMapping("/add")
       public String addUser(){
           User user=new User("李四",18);
           int res=userService.addUser(user);
           if(res>0) {
               return "添加成功";
           }
           return "添加失败";
       }
   
       @RequestMapping("getUser")
       public User getUserByName(){
           return userService.getUserByName("张三");
       }
   
       @RequestMapping("/getAllUser")
       public List<User> getAllUser(){
           return userService.getAllUser();
       }
   }
   ```

7. 测试

##### 5.1.2 使用Druid数据源

druid官方github地址：https://github.com/alibaba/druid

整合第三方技术的两种方式

- 自定义
- 找starter

###### 1、自定义方式

> 注：本节代码是在上一节代码上进行修改的

1. 导入依赖

   ```java
   <dependency>
       <groupId>com.alibaba</groupId>
       <artifactId>druid</artifactId>
       <version>1.1.17</version>
   </dependency>
   ```

2. 创建数据源

   创建配置类MyDataSourceConfig，将数据源添加到容器中

   ```java
   @Configuration
   public class MyDataSourceConfig {
   
       @Bean
       @ConfigurationProperties(prefix = "spring.datasource")  //将DataSource中属性与配置文件进行绑定
       public DataSource dataSource(){
           return new DruidDataSource();
       }
   }
   ```

3. 测试

**Druid数据源的其他功能**

1. 查看Druid的内置监控界面

   在配置类中添加如下内容

   ```java
   @Bean
       public ServletRegistrationBean statViewServlet(){
           StatViewServlet statViewServlet=new StatViewServlet();
           ServletRegistrationBean<StatViewServlet> registrationBean=new ServletRegistrationBean<>(statViewServlet,"/druid/*");
           return registrationBean;
       }
   ```

   启动springboot，进入浏览器访问http://localhost:8080/druid

   ![image-20210110195251923](SpringBoot.assets/image-20210110195251923.png)

2. 打开Druid的监控统计功能，查看SQL监控

   修改配置类数据源配置方法，配置_StatFilter

   ```java
   @Bean
   @ConfigurationProperties(prefix = "spring.datasource")  //将DataSource中属性与配置文件进行绑定
   public DataSource dataSource() throws SQLException {
       DruidDataSource druidDataSource=new DruidDataSource();
       //配置SQL监控功能
       druidDataSource.setFilters("stat");
       return druidDataSource;
   }
   ```

   测试

   ![image-20210110200422137](SpringBoot.assets/image-20210110200422137.png)

3. Web关联监控配置，查看Web应用和URI监控

   在配置类中添加如下内容

   ```java
   @Bean
       public FilterRegistrationBean webStatFilter(){
           WebStatFilter webStatFilter=new WebStatFilter();
           FilterRegistrationBean<WebStatFilter> registrationBean=new FilterRegistrationBean<WebStatFilter>(webStatFilter);
           registrationBean.setUrlPatterns(Arrays.asList("/*"));
           registrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
           return registrationBean;
       }
   ```

   

4. 等等，其他的可参考官方文档：https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98

###### 2、使用官方starter方式

1. 引入druid-starter

   ```java
           <dependency>
               <groupId>com.alibaba</groupId>
               <artifactId>druid-spring-boot-starter</artifactId>
               <version>1.1.17</version>
           </dependency>
   ```

   **分析自动配置**

   ![image-20210110232952020](SpringBoot.assets/image-20210110232952020.png)

   - 扩展配置项 **spring.datasource.druid**
   - DruidSpringAopConfiguration.**class**,  监控SpringBean的；配置项：**spring.datasource.druid.aop-patterns**
   - DruidStatViewServletConfiguration.**class**, 监控页的配置：**spring.datasource.druid.stat-view-servlet；默认开启**
   -  DruidWebStatFilterConfiguration.**class**, web监控配置；**spring.datasource.druid.web-stat-filter；默认开启**
   - DruidFilterConfiguration.**class**}) 所有Druid自己filter的配置

   ```java
       private static final String FILTER_STAT_PREFIX = "spring.datasource.druid.filter.stat";
       private static final String FILTER_CONFIG_PREFIX = "spring.datasource.druid.filter.config";
       private static final String FILTER_ENCODING_PREFIX = "spring.datasource.druid.filter.encoding";
       private static final String FILTER_SLF4J_PREFIX = "spring.datasource.druid.filter.slf4j";
       private static final String FILTER_LOG4J_PREFIX = "spring.datasource.druid.filter.log4j";
       private static final String FILTER_LOG4J2_PREFIX = "spring.datasource.druid.filter.log4j2";
       private static final String FILTER_COMMONS_LOG_PREFIX = "spring.datasource.druid.filter.commons-log";
       private static final String FILTER_WALL_PREFIX = "spring.datasource.druid.filter.wall";
   ```

   

2. 添加配置

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/db_account
       username: root
       password: 123456
       driver-class-name: com.mysql.jdbc.Driver
   
       druid:
         aop-patterns: com.atguigu.admin.*  #监控SpringBean
         filters: stat,wall     # 底层开启功能，stat（sql监控），wall（防火墙）
   
         stat-view-servlet:   # 配置监控页功能
           enabled: true    #druid提供的功能默认是关闭的，使用时先打开
           login-username: admin
           login-password: admin
           resetEnable: false
   
         web-stat-filter:  # 监控web
           enabled: true
           urlPattern: /*
           exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
   
   
         filter:
           stat:    # 对上面filters里面的stat的详细配置
             slow-sql-millis: 1000
             logSlowSql: true
             enabled: true
           wall:
             enabled: true
             config:
               drop-table-allow: false
   ```

#### 5.2 整合MyBatis 

https://github.com/mybatis

SpringBoot官方的Starter：spring-boot-starter-*

第三方的： *-spring-boot-starter

导入依赖

```java
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.4</version>
</dependency>
```

##### 5.2.1 配置模式

在application.xml添加mybatis配置规则

```
#mybatis配置规则
mybatis:
  #全局配置文件位置，可以不写，所有配置都在这里配置
  #config-location: classpath:com/mybatis-config.xml
  #mapper映射文件位置(包名应与XXXMapper类所在包名相同，否则会报BindingException）
  mapper-locations: classpth:com/kyn/mapper/*.xml
```

创建xxMapper.xml和xxMapper类，xxMapper.xml在resources下，并与xxMapper所在包名相同

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.kyn.mapper.UserMapper">
    <select id="getUser" resultType="com.kyn.pojo.User">
        select * from userinfo where name=#{name}
    </select>
</mapper>
```

```java
package com.kyn.mapper;

import com.kyn.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface UserMapper {

    public User getUser(String name);
}

```

编写service和controller

```java
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    public User getUser(String name){
        return userMapper.getUser(name);
    }

}
```

```

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/user")
    public User getUser(@RequestParam("name") String name){
        return userService.getUser(name);
    }
}

```

测试

##### 5.2.2 注解模式



#### 5.3 整合MyBatis-Plus



## 6.Spring Boot整合NoSQL





