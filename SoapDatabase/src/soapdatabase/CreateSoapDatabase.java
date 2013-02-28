/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soapdatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 *
 * @author Lee Holdsworth Date May 2012 Project DBSD352
 */
public class CreateSoapDatabase {

    private Properties dbProperties = null;
    private String dbName;
    private static final String strCreateIngredientTable =
            "create table APP.INGREDIENT ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    DESCR    VARCHAR(30), "
            + "    NAME     VARCHAR(30), "
            + "    QUANTITY   DOUBLE PRECISION, "
            + "    PRICE  DOUBLE PRECISION, "
            + "    OILPROPID     INTEGER, "
            + "    TYPE       INTEGER, "
            + "    SUPPLIERID    INTEGER, "
            + "    PURCHASEDATE  DATE, "
            + "    QOH  DOUBLE PRECISION "
            + ")";
    private static final String strCreateOilPropsTable =
            "create table APP.OILPROPS ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    SAP       DOUBLE PRECISION, "
            + "    SPGRAV       DOUBLE PRECISION "
            + ")";
    private static final String strCreateSupplierTable =
            "create table APP.SUPPLIER ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    NAME       VARCHAR(30), "
            + "    ADDRESS    VARCHAR(30), "
            + "    EMAIL       VARCHAR(30), "
            + "    WEB       VARCHAR(30) "
            + ")";
    private static final String strCreateTypeTable =
            "create table APP.TYPE ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    NAME       VARCHAR(30) "
            + ")";
    private static final String strCreateEssOilTable =
            "create table APP.ESSENTIALOIL ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    TYPEID      INTEGER, "
            + "    NAME       VARCHAR(30) "
            + ")";
    private static final String strCreateTextureTable =
            "create table APP.TEXTURE ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    TYPEID      INTEGER, "
            + "    NAME       VARCHAR(30) "
            + ")";
    private static final String strCreateLiquidTable =
            "create table APP.LIQUID ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    TYPEID      INTEGER, "
            + "    NAME       VARCHAR(30) "
            + ")";
    private static final String strCreateColorTable =
            "create table APP.COLOR ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    TYPEID      INTEGER, "
            + "    NAME       VARCHAR(30) "
            + ")";
    private static final String strCreateBaseOilTable =
            "create table APP.BASEOIL ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    TYPEID      INTEGER, "
            + "    NAME       VARCHAR(30) "
            + ")";
    private static final String strCreateRecipeTable =
            "create table APP.RECIPE ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    NAME      VARCHAR(30), "
            + "    METHOD       VARCHAR(90), "
            + "    COMMENT       VARCHAR(70) "
            + ")";
    private static final String strCreateRecipeIngredientTable =
            "create table APP.RECIPEINGREDIENT ("
            + "    ID          INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
            + "    RECIPEID           INTEGER, "
            + "    QUANTITY       DOUBLE PRECISION, "
            + "    INGREDIENTID       INTEGER "
            + ")";
   

    /**
     * CreateSoapDatabase Constructor
     */
    public CreateSoapDatabase() {
        this("defaultSoapmakerDb");
    }

    /**
     * Constructor
     *
     * @param soapmakerDb
     */
    public CreateSoapDatabase(String soapmakerDb) {
        this.dbName = soapmakerDb;
        setDBSystemDir();
        dbProperties = loadDBProperties();
        String driverName = dbProperties.getProperty("derby.driver");
        loadDatabaseDriver(driverName);
        if (!dbExists()) {
           // System.out.println(strCreateIngredientTable);
           // System.out.println(strCreateBaseOilTable);
            createDatabase();
        }

    }

    /**
     * dbExists does db exist
     *
     * @return true if db exists
     */
    private boolean dbExists() {
        boolean bExists = false;
        String dbLocation = getDatabaseLocation();
        File dbFileDir = new File(dbLocation);
        if (dbFileDir.exists()) {
            bExists = true;
        }
        return bExists;
    }

    /**
     * getProperties of db
     *
     * @return dbProperties
     */
    public Properties getProperties() {
        if (dbProperties == null) {
            System.out.printf("Error:properties is null.");
        }
        return dbProperties;
    }

    /**
     * setDBSystemDir set the path to the db
     */
    private void setDBSystemDir() {
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/.soapmaker";
        System.setProperty("derby.system.home", systemDir);

        // create the db system directory
        File fileSystemDir = new File(systemDir);
        fileSystemDir.mkdir();
        System.out.printf("Printing systemdir:%s\n", systemDir);
    }

    /**
     * createDatabase
     *
     * @return true if created else false
     */
    private boolean createDatabase() {
        boolean bCreated = false;
        Connection dbConnection = null;

        String dbUrl = getDatabaseUrl();
        dbProperties.put("create", "true");

        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bCreated = createTables(dbConnection);
        } catch (SQLException ex) {
        }
        dbProperties.remove("create");
        return bCreated;
    }

    /**
     * getHomeDir
     *
     * @return the home directory
     */
    private String getHomeDir() {
        return System.getProperty("user.home");
    }

    /**
     * getDatabaseLocation
     *
     * @return location path to database
     */
    public String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "\\" + dbName;
        return dbLocation;
    }

    /**
     * getDatabaseUrl
     *
     * @return url
     */
    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + dbName;
        return dbUrl;
    }

    /**
     * loadDBProperties
     *
     * @return dbProperties
     */
    private Properties loadDBProperties() {
        InputStream dbPropInputStream = null;
        dbPropInputStream =
                CreateSoapDatabase.class.getResourceAsStream("Configuration.properties");
        //SoapmakerDao.class.getResourceAsStream("Configuration.properties");
        if (dbPropInputStream == null) {
            System.out.printf("error getting resource\n");
        }
        dbProperties = new Properties();
        try {
            dbProperties.load(dbPropInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dbProperties;
    }

    /**
     * loadDatabaseDriver load driver given driverName
     *
     * @param driverName
     */
    private void loadDatabaseDriver(String driverName) {
        //load the Java DB driver
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * createTables
     *
     * @param dbConnection
     * @return true if tables created
     */
    private boolean createTables(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(strCreateIngredientTable);
            statement.execute(strCreateOilPropsTable);
            statement.execute(strCreateSupplierTable);
            statement.execute(strCreateTypeTable);
            statement.execute(strCreateEssOilTable);
            statement.execute(strCreateTextureTable);
            statement.execute(strCreateLiquidTable);
            statement.execute(strCreateColorTable);
            statement.execute(strCreateBaseOilTable);
            statement.execute(strCreateRecipeIngredientTable);
            statement.execute(strCreateRecipeTable);
            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return bCreatedTables;
    }
    
   

    public static void main(String[] args) {

       // CreateSoapDatabase db = new CreateSoapDatabase();

        // System.out.printf("%s\n",db.strCreateIngredientTable);


      //  System.out.println(db.getDatabaseLocation());
      //  System.out.println(db.getDatabaseUrl());
        
        
       // SoapManager.prefillTables();
       // System.out.printf("%s\n", db.strCreateIngredientTable);
        //System.out.printf("%s\n",db.strCreateSupplierTable);
        
    }
}//end class
