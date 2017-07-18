package ru.ifmo.ctddev.FSSARecSys.db.utils;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import ru.ifmo.ctddev.FSSARecSys.db.tables.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;


public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {

        if (sessionFactory != null) {
            return sessionFactory;
        }
//        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);

        Migrations.apply();

        Configuration configuration = new Configuration();

        configuration.addAnnotatedClass(DatasetRecord.class);
        configuration.addAnnotatedClass(DatasetMetaFeatureRecord.class);
        configuration.addAnnotatedClass(FeatureSubsetSelectionAlgorithmRecord.class);
        configuration.addAnnotatedClass(MetaFeatureExtractorRecord.class);
        configuration.addAnnotatedClass(MetaLearningAlgorithmRecord.class);
        configuration.addAnnotatedClass(MetaLearningAlgorithmEvaluationResultRecord.class);
        configuration.addAnnotatedClass(SelectedFeaturesRecord.class);

        configuration.configure();
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());

        sessionFactory = configuration.buildSessionFactory(builder.build());

        return sessionFactory;
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    public static <T> T runQuery(Function<Session, T> function) {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            session.beginTransaction();
            return function.apply(session);

        } finally {
            if (session != null) {
                session.getTransaction().commit();
                session.close();
            }

//            close();
        }
    }

    public static void runQueryVoid(Consumer<Session> function) {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            session.beginTransaction();
            function.accept(session);

        } finally {
            if (session != null) {
                session.getTransaction().commit();
                session.close();
            }

//            close();
        }
    }
}
