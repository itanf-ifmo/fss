package ru.ifmo.ctddev.FSSARecSys.db.utils;

import ru.ifmo.ctddev.FSSARecSys.db.tables.VersionRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.SQLGrammarException;


public class Migrations {
    private static String[] migrations = {
            // create hibernate database table
            "CREATE TABLE `hibernate_sequence` (\n" +
            "`next_val` BIGINT(20) DEFAULT NULL\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",
            "INSERT INTO `hibernate_sequence` VALUES (1),(1),(1),(1),(1);",

            // create version table
            "CREATE TABLE `Version` (\n" +
            "`id` int(11) NOT NULL,\n" +
            "`version` int(11) NOT NULL,\n" +
            "PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create Dataset table
            "CREATE TABLE `Dataset` (" +
            "  `id` INT(11) NOT NULL," +
            "  `file` LONGBLOB NOT NULL," +
            "  `name` VARCHAR(255) NOT NULL," +
            "  `taskType` VARCHAR(255) NOT NULL," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `UK_qsap3d0f0yb3yke640vpn15bv` (`name`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create FSSAlgorithm table
            "CREATE TABLE `FSSAlgorithm` (" +
            "  `id` INT(11) NOT NULL," +
            "  `evalClass` VARCHAR(255) NOT NULL," +
            "  `evalOptions` VARCHAR(255) NOT NULL," +
            "  `name` VARCHAR(255) NOT NULL," +
            "  `searchClass` VARCHAR(255) NOT NULL," +
            "  `searchOptions` VARCHAR(255) NOT NULL," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `UK_ph0us5qfrw7idpsf9fdofkvd3` (`name`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create MLAlgorithm table
            "CREATE TABLE `MLAlgorithm` (" +
            "  `id` INT(11) NOT NULL," +
            "  `classPath` VARCHAR(255) NOT NULL," +
            "  `name` VARCHAR(255) NOT NULL," +
            "  `options` VARCHAR(255) NOT NULL," +
            "  `taskType` VARCHAR(255) NOT NULL," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `UK_rjyt8o29r98loj061q3erjut` (`name`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create MLParameter table
            "CREATE TABLE `MLParameter` (" +
            "  `id` INT(11) NOT NULL," +
            "  `name` VARCHAR(255) NOT NULL," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `UK_itp8gnoa6iay56x3gf24d4mnd` (`name`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create MetaFeatureExtractor table
            "CREATE TABLE `MetaFeatureExtractor` (" +
            "  `id` INT(11) NOT NULL," +
            "  `classPath` VARCHAR(255) NOT NULL," +
            "  `name` VARCHAR(255) NOT NULL," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `UK_92gvjp3ycd9hy1k90lgj8yd0q` (`name`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create MetaLearningResult table
            "CREATE TABLE `SelectedFeatures` (" +
            "  `selected_features` LONGBLOB NOT NULL," +
            "  `fss_algorithm` INT(11) NOT NULL," +
            "  `dataset` INT(11) NOT NULL," +
            "  PRIMARY KEY (`fss_algorithm`,`dataset`)," +
            "  KEY `FKnarpesj5jds93hbsoa9qcy4i6` (`dataset`)," +
            "  CONSTRAINT `FKnarpesj5jds93hbsoa9qcy4i6` FOREIGN KEY (`dataset`) REFERENCES `Dataset` (`id`)," +
            "  CONSTRAINT `FKq91yv6py4ksi8766aosm9x7bp` FOREIGN KEY (`fss_algorithm`) REFERENCES `FSSAlgorithm` (`id`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create DatasetMetaFeature table
            "CREATE TABLE `DatasetMetaFeature` (" +
            "  `value` DOUBLE NOT NULL," +
            "  `meta_feature_extractor` INT(11) NOT NULL," +
            "  `dataset` INT(11) NOT NULL," +
            "  PRIMARY KEY (`meta_feature_extractor`,`dataset`)," +
            "  KEY `FKr8oikbp3a8wy9wtbx6sel024k` (`dataset`)," +
            "  CONSTRAINT `FKm1fskxfr2d66jn8lrs8ca249q` FOREIGN KEY (`meta_feature_extractor`) REFERENCES `MetaFeatureExtractor` (`id`)," +
            "  CONSTRAINT `FKr8oikbp3a8wy9wtbx6sel024k` FOREIGN KEY (`dataset`) REFERENCES `Dataset` (`id`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            // create MLEvaluationResult table
            "CREATE TABLE `MLEvaluationResult` (" +
            "  `result` LONGBLOB NOT NULL," +
            "  `ml_parameter` INT(11) NOT NULL," +
            "  `ml_algorithm` INT(11) NOT NULL," +
            "  `dataset` INT(11) NOT NULL," +
            "  `fss_algorithm` INT(11) NOT NULL," +
            "  PRIMARY KEY (`fss_algorithm`,`dataset`,`ml_parameter`,`ml_algorithm`)," +
            "  KEY `FK8ab2q1u1qhgwsby7r97wp2ruy` (`ml_parameter`)," +
            "  KEY `FKj2yo2ip7vj2s5hmm9uhvu6jou` (`ml_algorithm`)," +
            "  KEY `FK31kjaievwqrv7w0110oyqwy9m` (`dataset`)," +
            "  CONSTRAINT `FK31kjaievwqrv7w0110oyqwy9m` FOREIGN KEY (`dataset`) REFERENCES `Dataset` (`id`)," +
            "  CONSTRAINT `FK8ab2q1u1qhgwsby7r97wp2ruy` FOREIGN KEY (`ml_parameter`) REFERENCES `MLParameter` (`id`)," +
            "  CONSTRAINT `FKec05cbtv7h6wsedt9fnxgrltd` FOREIGN KEY (`fss_algorithm`, `dataset`) REFERENCES `SelectedFeatures` (`fss_algorithm`, `dataset`)," +
            "  CONSTRAINT `FKi4aw9n9ils8u14o0tgms6hqi1` FOREIGN KEY (`fss_algorithm`) REFERENCES `FSSAlgorithm` (`id`)," +
            "  CONSTRAINT `FKj2yo2ip7vj2s5hmm9uhvu6jou` FOREIGN KEY (`ml_algorithm`) REFERENCES `MLAlgorithm` (`id`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            "ALTER TABLE `SelectedFeatures` ADD COLUMN `selectionTime` DOUBLE NOT NULL",
            "ALTER TABLE MLEvaluationResult DROP FOREIGN KEY FK8ab2q1u1qhgwsby7r97wp2ruy;",
            "DROP INDEX FK8ab2q1u1qhgwsby7r97wp2ruy ON MLEvaluationResult;",
            "ALTER TABLE MLEvaluationResult DROP ml_parameter;",
            "DROP TABLE MLParameter;",
    };

    private static SessionFactory sessionFactory;

    private static void setup() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(VersionRecord.class);
        configuration.configure();

        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());

        sessionFactory = configuration.buildSessionFactory(builder.build());
    }

    public static void apply() {
        if (sessionFactory != null) {
            return;  // already applied
        }

        setup();

        VersionRecord VersionRecord = null;
        try {
            Session session = sessionFactory.openSession();
            VersionRecord = session.get(VersionRecord.class, 0);
            session.close();
        } catch (SQLGrammarException e) {
            String exceptionString = e.getCause().toString();
            // in case if database doesn`t exists
            if (exceptionString.contains(": Table '") && exceptionString.endsWith("doesn't exist")) {
                VersionRecord = null;
            } else {
                throw e;
            }
        }

        if (VersionRecord == null) {
            migrate(0);
            System.out.println(String.format("Database schema migrations applied successfully: from %d to %d", 0, migrations.length));  // todo logger
        } else {
            if (VersionRecord.getVersion() == migrations.length) {
                System.out.println("Database schema up-to-date");  // todo logger
            } else {
                System.out.println(String.format("Database schema migrations applied successfully: from %d to %d", VersionRecord.getVersion(), migrations.length));  // todo logger
                migrate(VersionRecord.getVersion());
            }
        }

        sessionFactory.close();
    }

    private static void migrate(int from) {
        Session session = sessionFactory.openSession();

        session.close();
        session = sessionFactory.openSession();
        session.beginTransaction();

        for (int i = from; i < migrations.length; i++) {
            System.out.println("Applying migration: " + migrations[i]);  // todo logger
            session.createSQLQuery(migrations[i]).executeUpdate();
        }

        session.getTransaction().commit();
        session.close();
        session = sessionFactory.openSession();
        session.beginTransaction();

        VersionRecord VersionRecord = session.get(VersionRecord.class, 0);

        if (VersionRecord == null) {
            VersionRecord = new VersionRecord();
            VersionRecord.setId(0);
            VersionRecord.setVersion(migrations.length);
            session.save(VersionRecord);
        } else {
            VersionRecord.setVersion(migrations.length);
            session.update(VersionRecord);
        }

        session.getTransaction().commit();
        session.close();
    }
}
