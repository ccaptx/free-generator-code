package com.my.ffc.plugin;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FfcPlugin extends BasePlugin {

    private String tableTemplatePackage;
    private String fieldTemplatePackage;
    private Configuration cfgTable = new Configuration(Configuration.VERSION_2_3_23);
    private Configuration cfgField = new Configuration(Configuration.VERSION_2_3_23);

    @Override
    public void setContext(Context context) {
        try {
            tableTemplatePackage = context.getProperty("tableTemplatePackage");
            fieldTemplatePackage = context.getProperty("fieldTemplatePackage");

            cfgTable.setDirectoryForTemplateLoading(new File(tableTemplatePackage));
            cfgField.setDirectoryForTemplateLoading(new File(fieldTemplatePackage));
        } catch (IOException e) {
            throw new RuntimeException("error!!!!!!!!!!!!执行失败!!!!!!!!!!!!!!!!!!!", e);
        }

    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        readyGo(PluginTypeEnum.FIELD, field, topLevelClass, introspectedColumn, introspectedTable, modelClassType, null);
        return false;
    }

    @Override
    public boolean clientGenerated(Interface anInterface, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        readyGo(PluginTypeEnum.TABLE, null, topLevelClass, null, introspectedTable, null, anInterface);
        return false;
    }

    private void readyGo(PluginTypeEnum pluginTypeEnum, Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType, Interface anInterface) {

        // 建立数据模型（Map）
        Map<String, Object> commonMap = new HashMap<>();
        commonMap.put("field", field);
        commonMap.put("class", topLevelClass);
        commonMap.put("column", introspectedColumn);
        commonMap.put("table", introspectedTable);
        commonMap.put("classType", modelClassType);
        commonMap.put("interface", anInterface);

        try {

            String templatePackage;
            Template template;
            switch (pluginTypeEnum) {
                case TABLE:
                    templatePackage = tableTemplatePackage;
                    template = cfgTable.getTemplate("aa.ftl");
                    break;
                case FIELD:
                    templatePackage = fieldTemplatePackage;
                    template = cfgField.getTemplate("aa.ftl");
                    break;
                default:
                    throw new RuntimeException("未匹配到类型!!!!!!!!!!!!执行失败!!!!!!!!!!!!!!!!!!!");
            }

            ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
            Writer out = new OutputStreamWriter(bao);
            template.process(commonMap, out);
            out.flush();
            out.close();

            String s = new String(bao.toByteArray(), StandardCharsets.UTF_8);
            System.out.println(":::::::::::::::::::::::::::::::::::" + s);

            Properties ftlProperties = new Properties();
            ftlProperties.load(new StringReader("#设置表触发ftl模板文件夹\n" +
                    "tableTemplatePackage=src/main/resources/ffcTemplate/defaultGroup/tableTemplate\n" +
                    "#设置属性触发ftl模板文件夹(没有的话，可以不设置)\n" +
                    "fieldTemplatePackage=src/main/resources/ffcTemplate/defaultGroup/fieldTemplate\n"));
            System.out.println("666666666666666666");

        } catch (Exception e) {
            throw new RuntimeException("error!!!!!!!!!!!!执行失败!!!!!!!!!!!!!!!!!!!", e);
        }

    }

    public static void main(String[] args) throws IOException {
        Path path = new File("D:/a/b/d").toPath();
        Files.createDirectories(path);
    }
}
