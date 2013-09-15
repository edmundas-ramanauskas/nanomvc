nanomvc
=======

minimal java mvc framework

compile project and add to your project lib's folder. also web.xml configuration is needed:

<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.0" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">
    <filter>
        <filter-name>EncodingFilter</filter-name>
        <filter-class>com.nanomvc.EncodingFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>EncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    <servlet>
        <servlet-name>Bootstrap</servlet-name>
        <servlet-class>com.nanomvc.Bootstrap</servlet-class>
        <init-param>
            <param-name>viewsPath</param-name>
            <param-value>/WEB-INF/views</param-value>
        </init-param>
        <init-param>
            <param-name>controllersPath</param-name>
            <param-value>org.path.to.your.controllers</param-value>
        </init-param>
        <init-param>
            <param-name>defaultController</param-name>
            <param-value>YourDefaultControllerClass</param-value>
        </init-param>
        <init-param>
            <param-name>routerClass</param-name>
            <param-value>org.yourPackage.YourRouterClass</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>/public/*</url-pattern>
        <url-pattern>/favicon.ico</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>Bootstrap</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
    
    <listener>
        <listener-class>com.nanomvc.ExecutorContextListener</listener-class>
    </listener>
</web-app>
