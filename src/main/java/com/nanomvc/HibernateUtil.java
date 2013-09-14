package com.nanomvc;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil
{
    private static Logger _log = LoggerFactory.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory;
    private static File configFile;

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            if (configFile != null) {
                configuration.configure(configFile);
            } else {
                configuration.configure();
            }
            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            _log.error(ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Properties getServiceConfig(String services) {
        Properties props = null;
        try {
            JSONObject json = new JSONObject(services);
            Iterator iter = json.keys();
            while (iter.hasNext()) {
                String key = iter.next().toString();
                if (key.startsWith("mysql")) {
                    props = new Properties();
                    String host = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("host");

                    String port = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("port");

                    String name = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("name");

                    String url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=UTF-8";

                    props.setProperty("hibernate.connection.url", url);

                    String username = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("username");

                    String password = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("password");

                    props.setProperty("hibernate.connection.username", username);
                    props.setProperty("hibernate.connection.password", password);
                }
            }
        } catch (JSONException ex) {
        }
        return props;
    }

    public static void setConfigurationFile(File file) {
        configFile = file;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static Session getSession() throws HibernateException {
        Session session = null;
        try {
            session = getSessionFactory().getCurrentSession();
            if (!session.isOpen()) {
                session = getSessionFactory().openSession();
            }
        } catch (HibernateException he) {
            session = getSessionFactory().openSession();
        }
        return session;
    }
}