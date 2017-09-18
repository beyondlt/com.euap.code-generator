package com.euap.code.generator;

import httl.Engine;
import httl.Template;
import httl.util.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LiuTao
 * Mybatis代码生成器
 */
public class Generation {

    private static final Logger logger = Logger.getLogger(Generation.class);

    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日");

    private final String POINT = ".";
    private final String COMMA = ",";
    private final String EMPTY = "";
    // 参数
    private Properties properties;

    GenConfig genConfig = new GenConfig();
    //
    private StringBuilder builder = new StringBuilder();
    //
    private StringBuilder baseColumnBuilder = new StringBuilder();
    //
    private Map<JdbcType, String> typeRegistry = new HashMap<JdbcType, String>();

    public enum Type {
        ALL("all"), XML("xml"), MAPPER("mapper"), MODEL("model");

        private String name;

        Type(String name) {
            this.name = name;
        }

        boolean equals(String name) {
            return this.name.equals(name);
        }
    }

    public Generation(Properties properties) {
        if (properties == null) {
            this.properties = System.getProperties();
        } else {
            this.properties = properties;
        }

        typeRegistry.put(JdbcType.CHAR, "String");
        typeRegistry.put(JdbcType.VARCHAR, "String");
        typeRegistry.put(JdbcType.LONGVARCHAR, "String");
        typeRegistry.put(JdbcType.NUMERIC, "java.math.BigDecimal");
        typeRegistry.put(JdbcType.DECIMAL, "java.math.BigDecimal");
        typeRegistry.put(JdbcType.BIT, "boolean");
        typeRegistry.put(JdbcType.BOOLEAN, "boolean");
        typeRegistry.put(JdbcType.TINYINT, "byte");
        typeRegistry.put(JdbcType.SMALLINT, "short");
        typeRegistry.put(JdbcType.INTEGER, "int");
        typeRegistry.put(JdbcType.BIGINT, "long");
        typeRegistry.put(JdbcType.REAL, "float");
        typeRegistry.put(JdbcType.FLOAT, "double");
        typeRegistry.put(JdbcType.DOUBLE, "double");
        typeRegistry.put(JdbcType.BINARY, "byte[]");
        typeRegistry.put(JdbcType.VARBINARY, "byte[]");
        typeRegistry.put(JdbcType.LONGVARBINARY, "byte[]");
        typeRegistry.put(JdbcType.DATE, "java.sql.Date");
        typeRegistry.put(JdbcType.TIME, "java.sql.Time");
        typeRegistry.put(JdbcType.TIMESTAMP, "java.sql.Timestamp");
        typeRegistry.put(JdbcType.CLOB, "java.sql.Clob");
        typeRegistry.put(JdbcType.BLOB, "java.sql.Blob");
        typeRegistry.put(JdbcType.ARRAY, "java.sql.Array");
        typeRegistry.put(JdbcType.STRUCT, "java.sql.Struct");

        init();
    }

    public void init() {

        // 数据库连接
        genConfig.dbType = properties.getProperty("jdbc.dbType", "oracle");
        genConfig.driverClassName = properties.getProperty("jdbc.driverClassName", "oracle.jdbc.driver.OracleDriver");
        genConfig.url = properties.getProperty("jdbc.url", EMPTY);
        genConfig.username = properties.getProperty("jdbc.username", EMPTY);
        genConfig.password = properties.getProperty("jdbc.password", EMPTY);

        // 生成配置
        genConfig.projectPath = trim(properties.getProperty("project.path", System.getProperty("user.dir")), 2, "\\", "/");
        genConfig.src = trim(properties.getProperty("project.src", "src"), "\\", "/");
        genConfig.tableNames = properties.getProperty("table.name", EMPTY).split(COMMA);
        genConfig.prefix = properties.getProperty("table.prefix", EMPTY);
        genConfig.modelPackage = trim(properties.getProperty("package.model", EMPTY), POINT);
        genConfig.xmlPackage = trim(properties.getProperty("package.xml", EMPTY), POINT);
        genConfig.mapperPackage = trim(properties.getProperty("package.mapper", EMPTY), POINT);
        genConfig.fileType = properties.getProperty("gen.fileType", "all");

        if (StringUtils.isBlank(genConfig.xmlPackage)) {
            genConfig.xmlPackage = genConfig.mapperPackage;
        } else if (StringUtils.isBlank(genConfig.mapperPackage)) {
            genConfig.mapperPackage = genConfig.xmlPackage;
        }

        genConfig.modelPath = genConfig.modelPackage.replace(POINT, File.separator);
        genConfig.mapperPath = genConfig.mapperPackage.replace(POINT, File.separator);
        genConfig.xmlPath = genConfig.xmlPackage.replace(POINT, File.separator);
    }

    private void generationFile() throws Exception {
        List<Map> configList = getMetaConfig(genConfig.tableNames);
        Properties engineConfig = new Properties();
        engineConfig.put("loaders+", "httl.spi.loaders.JarLoader");
        engineConfig.put("compiler", "httl.spi.compilers.JavassistCompiler");
        engineConfig.put("json.codec", "httl.spi.codecs.JacksonCodec");
        Engine engine = Engine.getEngine(engineConfig);
        try {
            Template xmlTemplate = engine.getTemplate("mybatis/xml.template", "gbk");
            Template mapperTemplate = engine.getTemplate("mybatis/mapper.template", "gbk");
            Template modelTemplate = engine.getTemplate("mybatis/model.template", "gbk");
            int length = configList.size();
            for (int i = 0; i < length; i++) {
                Map map = configList.get(i);
                try {
                    if (genFile(Type.MODEL)) {
                        String modelFileName = genConfig.projectPath.concat(File.separator).concat(genConfig.src).concat(File.separator)
                                .concat(genConfig.modelPath).concat(File.separator).concat(String.valueOf(map.get("javaName")).concat(".java"));
                        logger.info("Start generating file ".concat(modelFileName));
                        File f = new File(modelFileName);
                        if (!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        modelTemplate.render(map, new FileOutputStream(f));
                    }
                    if (genFile(Type.MAPPER)) {
                        String mapperFileName = genConfig.projectPath.concat(File.separator).concat(genConfig.src).concat(File.separator)
                                .concat(genConfig.mapperPath).concat(File.separator).concat(String.valueOf(map.get("javaName")).concat("Mapper.java"));
                        logger.info("Start generating file ".concat(mapperFileName));
                        File f = new File(mapperFileName);
                        if (!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        mapperTemplate.render(map, new FileOutputStream(f));
                    }
                    if (genFile(Type.XML)) {
                        String xmlFileName = genConfig.projectPath.concat(File.separator).concat(genConfig.src).concat(File.separator).concat(genConfig.xmlPath)
                                .concat(File.separator).concat(String.valueOf(map.get("javaName")).concat("Mapper.xml"));
                        logger.info("Start generating file ".concat(xmlFileName));
                        File f = new File(xmlFileName);
                        if (!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        xmlTemplate.render(map, new FileOutputStream(f));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Map> getMetaConfig(String[] tableNames) throws Exception {
        logger.info("Start loading the data tables: ".concat(Arrays.toString(tableNames)));
        Connection conn = null;
        List<Map> configList = new ArrayList<Map>();
        ResultSet tableResult = null;
        ResultSet keyResult = null;
        ResultSet columnResult = null;
        Properties props = new Properties();
        props.put("remarksReporting", "true");
        props.put("user", genConfig.username);
        props.put("password", genConfig.password);
        String date = sdf.format(new Date());
        try {
            Class.forName(genConfig.driverClassName);
            conn = DriverManager.getConnection(genConfig.url, props);
            DatabaseMetaData metaData = conn.getMetaData();
            int length = tableNames.length;
            for (int i = 0; i < length; i++) {
                baseColumnBuilder.delete(0, baseColumnBuilder.length());
                String tableName = tableNames[i];
                String javaName = dbConverJava(tableName);
                javaName = javaName.substring(0, 1).toUpperCase().concat(javaName.substring(1));
                Map map = new HashMap();
                map.put("genConfig", genConfig);
                map.put("date", date);
                map.put("tableName", tableName);
                map.put("javaName", javaName);
                map.put("mapperName", genConfig.mapperPackage.concat(POINT).concat(javaName).concat("Mapper"));
                map.put("modelName", genConfig.modelPackage.concat(POINT).concat(javaName));
                List<Map> columns = new ArrayList<Map>();
                List<Map<String, String>> keys = new ArrayList<Map<String, String>>();
                Set<String> pkSet = new HashSet<String>();

                tableResult = metaData.getTables(null, null, genConfig.tableNames[i], new String[] { "TABLE" });
                while (tableResult.next()) {
                    map.put("tableComments", tableResult.getString("REMARKS"));
                    logger.info("Got data table ".concat(tableName + "[" + tableResult.getString("REMARKS") + "]"));
                }

                keyResult = metaData.getPrimaryKeys(null, null, genConfig.tableNames[i]);
                while (keyResult.next()) {
                    String columnName = keyResult.getString("COLUMN_NAME");
                    pkSet.add(columnName);
                }

                columnResult = metaData.getColumns(null, null, genConfig.tableNames[i], "%");
                while (columnResult.next()) {
                    Map<String, String> column = new HashMap<String, String>();
                    String columnName = columnResult.getString("COLUMN_NAME");
                    baseColumnBuilder.append("t1.").append(columnName).append(COMMA);
                    column.put("columnName", columnName);
                    String propertyName = dbConverJava(columnName);
                    column.put("propertyName", propertyName);
                    column.put("setMethodName", "set".concat(propertyName.substring(0, 1).toUpperCase().concat(propertyName.substring(1))));
                    column.put("getMethodName", "get".concat(propertyName.substring(0, 1).toUpperCase().concat(propertyName.substring(1))));
                    column.put("jdbcType", JdbcType.forCode(columnResult.getInt("DATA_TYPE")).name());
                    column.put("javaType", typeRegistry.get(JdbcType.forCode(columnResult.getInt("DATA_TYPE"))));
                    column.put("remark", columnResult.getString("REMARKS"));
                    columns.add(column);
                    // 记录主键信息
                    if (pkSet.contains(columnName)) {
                        column.put("pkMark", "（PK）");
                        keys.add(column);
                    }
                    logger.info("Got column " + columnName + (column.get("pkMark") == null ? "" : column.get("pkMark")) + "[" + column.get("remark") + "]");
                }
                map.put("baseColumnList", baseColumnBuilder.substring(0, baseColumnBuilder.length() - 1).toString());
                map.put("columns", columns);

                map.put("primaryKeys", keys);
                map.put("pkMethodParameters", getPKMethodParameters(keys));

                configList.add(map);
            }
            logger.info("Load the data table to complete.");
        } catch (Exception e) {
            throw e;
        } finally {
            if(columnResult != null) {
                try {
                    columnResult.close();
                } catch (Exception e1) {
                }
                columnResult = null;
            }
            if(keyResult != null) {
                try {
                    keyResult.close();
                } catch (Exception e1) {
                }
                keyResult = null;
            }
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
                conn = null;
            }
        }
        return configList;
    }

    private String getPKMethodParameters(List<Map<String, String>> keys) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map<String, String> map : keys) {
            String pn = map.get("propertyName");
            if (StringUtils.isBlank(pn)) {
                continue;
            }
            if (i++ > 0) {
                sb.append(", ");
            }
            sb.append("@Param(\"").append(pn).append("\") String ").append(pn);
        }

        return sb.toString();
    }

    private boolean genFile(Type fileType) {
        if (!Type.ALL.equals(genConfig.fileType)) {
            return true;
        }
        return fileType.equals(genConfig.fileType);
    }

    private String dbConverJava(String converName) {
        if (genConfig.prefix != null && converName.startsWith(genConfig.prefix)) {
            converName = converName.substring(genConfig.prefix.length());
        }
        String[] names = converName.split("_");
        int length = names.length;
        this.builder.delete(0, this.builder.length());
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                this.builder.append(names[i].toLowerCase());
            } else {
                this.builder.append(names[i].substring(0, 1).toUpperCase().concat(names[i].substring(1).toLowerCase()));
            }
        }
        return this.builder.toString();
    }

    public static void main(String[] args) {
        logger.info("Start generate mybatis files.");
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("mybatisGen.properties");
            Properties properties = System.getProperties();
            properties.load(is);
            Generation generation = new Generation(properties);
            logger.info("Load the configuration file to complete.");
            generation.generationFile();
            logger.info("Generate complete.");
        } catch (IOException e) {
            logger.error("Generate failed.", e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Generate failed.", e);
            e.printStackTrace();
        }
    }

    public static String trim(String s, int position, String... ts) {
        if (s == null || ts == null || ts.length == 0) {
            return s;
        }

        s = s.trim();

        int i = 0;
        while (i < ts.length) {
            String t = ts[i];
            i++;
            if (t == null || t.length() == 0) {
                continue;
            }

            int l = t.length();

            while ((position & 1) == 1 && s.startsWith(t)) {
                s = s.substring(l);
                i = i == 1 ? 1 : 0;
            }

            while ((position & 2) == 2 && s.endsWith(t)) {
                s = s.substring(0, s.length() - l);
                i = i == 1 ? 1 : 0;
            }
        }

        return s;
    }
    public static String trim(String s, String... ts) {
        return trim(s, 3, ts);
    }

}
