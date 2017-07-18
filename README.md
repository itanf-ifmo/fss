#### Setup database:
1) Create database
2) Get user and password for user that hibernate will use to connect to database
3) Copy `Database/src/main/resources/hibernate.cfg.xml.example` to your recourse folder and remove suffix `.example`. Framework should be able to find and load this configuration file now.
4) Modify hibernate.cfg: replace username, password and database name (current database name is `FSSRECSYS`)
5) Configure mysql database(increase `max_allowed_packet` and `innodb_log_file_size`) in server conf (for me config is `/etc/mysql/my.cnf`). Modify following lines:
```
[mysqld]
innodb_log_file_size                    = 512M  
max_allowed_packet                      = 256M
```

You don't need to worry about tables: they will be created on first run of the framework.
To do so frammework will run all migrations sequentially from file `Database/src/main/java/ru/ifmo/ctddev/FSSARecSys/db/utils/Migrations.java`
If you need to change schema of the database just add new migration command in migrations file and run any database related code.


# todos:

 * Get rid of `Found class weka.core.Instance, but interface was expected` errors for packages:
   * `nz.ac.waikato.cms.weka:probabilisticSignificanceAE:1.0.2`
   * `nz.ac.waikato.cms.weka:fastCorrBasedFS:1.0.2`
   
 * Get rid of double type in database
 * optimize min-max query for meta features (do a one query)
 * reorganize search of nearest package
 * hide class benchmarks from database interface
 * rename SelectedFeaturesRecord class (e.g.: FeatureSelectionResultRecord)
 * get red of Class<? extends MetaLearningAlgorithmOutput> clazz, Class<? extends MetaLearningAlgorithm> metaLearningAlgorithmClazz, Class<? extends FeatureSelectionResult> featureSelectionResultClazz, Class<? extends Dataset> datasetClazz, Class<? extends FeatureSubsetSelectionAlgorithm> fssAlgorithmClazz
 * task type is String!???!?!??!
