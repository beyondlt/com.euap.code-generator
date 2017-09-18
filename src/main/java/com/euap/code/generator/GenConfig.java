package com.euap.code.generator;

/**
 * 生成器参数
 */
public class GenConfig {
    // 项目所在路径
    String projectPath;
    // 项目资源路径
    String src;
    //要生成Mybatis代码的数据表数组
    String[] tableNames;
    // 前缀，生成类时需要去除的前缀字符
    String prefix;
    // 生成文件类型
    String fileType;
    // 接口包名
    String mapperPackage;
    // 接口物理路径
    String mapperPath;
    // xml包名
    String xmlPackage;
    // xml物理路径
    String xmlPath;
    // 实体类包名
    String modelPackage;
    // 实体类物理路径
    String modelPath;
    // 数据库类型
    String dbType;
    // 数据库驱动类
    String driverClassName;
    // 数据库链接地址
    String url;
    // 数据库用户名
    String username;
    // 数据库密码
    String password;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMapperPackage() {
        return mapperPackage;
    }

    public void setMapperPackage(String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }

    public String getMapperPath() {
        return mapperPath;
    }

    public void setMapperPath(String mapperPath) {
        this.mapperPath = mapperPath;
    }

    public String getXmlPackage() {
        return xmlPackage;
    }

    public void setXmlPackage(String xmlPackage) {
        this.xmlPackage = xmlPackage;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getModelPackage() {
        return modelPackage;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
