package com.nanomvc;

import java.io.File;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
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
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
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