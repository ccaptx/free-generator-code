package com.my.ffc.plugin;

import com.my.ffc.xml.IgnoreDTDEntityResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FfcPlugin extends BasePlugin {

    /**
     * @author wulm
     * @date 2019/5/26 21:06
     * @version 1.0.0
     * @desc 插件类型枚举
     */
    public enum PluginTypeEnum {
        TABLE,
        FIELD
    }

    private String tableTemplatePackage;
    private String fieldTemplatePackage;
    private String[] replaceModeCheckAttributes;
    private Configuration cfgTable = new Configuration(Configuration.VERSION_2_3_23);
    private Configuration cfgField = new Configuration(Configuration.VERSION_2_3_23);
    private boolean isEnableTablePlugin = true;
    private boolean isEnableFieldPlugin = true;

    @Override
    public void setContext(Context context) {
        try {

            tableTemplatePackage = context.getProperty("tableTemplatePackage");
            fieldTemplatePackage = context.getProperty("fieldTemplatePackage");
            String rmca = context.getProperty("replaceModeCheckAttributes");

            if ("${tableTemplatePackage}".equals(tableTemplatePackage)) {
                tableTemplatePackage = null;
                isEnableTablePlugin = false;
            } else {
                Assert.isTrue(Files
                        .isDirectory(new File(tableTemplatePackage)
                                .toPath()), "确保tableTemplatePackage存在且为文件夹！tableTemplatePackage=" + tableTemplatePackage);
                cfgTable.setDirectoryForTemplateLoading(new File(tableTemplatePackage));
            }

            if ("${fieldTemplatePackage}".equals(fieldTemplatePackage)) {
                fieldTemplatePackage = null;
                isEnableFieldPlugin = false;
            } else {
                Assert.isTrue(Files
                        .isDirectory(new File(fieldTemplatePackage)
                                .toPath()), "确保fieldTemplatePackage存在且为文件夹！fieldTemplatePackage=" + fieldTemplatePackage);
                cfgField.setDirectoryForTemplateLoading(new File(fieldTemplatePackage));

            }

            if ("${replaceModeCheckAttributes}".equals(rmca)) {
                rmca = "id";
            }
            replaceModeCheckAttributes = rmca.split(",");

        } catch (IOException e) {
            throw new RuntimeException("执行失败!!!!!!!!!!!!!!!!!!!", e);
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
        commonMap.put("topLevelClass", topLevelClass);
        commonMap.put("introspectedColumn", introspectedColumn);
        commonMap.put("introspectedTable", introspectedTable);
        commonMap.put("modelClassType", modelClassType);
        commonMap.put("anInterface", anInterface);

        try {

            Configuration cfg;
            String templatePackage;
            switch (pluginTypeEnum) {
                case TABLE:
                    if (!isEnableTablePlugin) {
                        return;
                    }
                    cfg = cfgTable;
                    templatePackage = tableTemplatePackage;
                    break;
                case FIELD:
                    if (!isEnableFieldPlugin) {
                        return;
                    }
                    cfg = cfgField;
                    templatePackage = fieldTemplatePackage;
                    break;
                default:
                    throw new RuntimeException("未匹配到类型!!!!!!!!!!!!执行失败!!!!!!!!!!!!!!!!!!!");
            }

            Files.list(new File(templatePackage).toPath()).forEach(path -> {

                if (path.toAbsolutePath().toString().endsWith(".ftl")) {

                    try {
                        Path configFilePath = new File(path.toAbsolutePath().toString() + "config.properties").toPath();

                        //从ftl模板生成properties内容
                        byte[] baoBytesProperties = loadFtlTemplate(commonMap, cfg, configFilePath);
                        //生成properties
                        Properties ftlProperties = new Properties();
                        ftlProperties.load(new StringReader(new String(baoBytesProperties, StandardCharsets.UTF_8)));

                        //properties配置属性
                        String filePathProp = ftlProperties.getProperty("filePath");
                        Assert.isTrue(!StringUtils.isEmpty(filePathProp), configFilePath.toAbsolutePath()
                                .toString() + "文件中filePath不能为空");
                        String fileCreateTypeProp = ftlProperties.getProperty("fileCreateType");

                        //从ftl模板生成代码
                        byte[] baoBytesCode = loadFtlTemplate(commonMap, cfg, path);

                        Path createFilePath = new File(filePathProp).toPath();
                        //创建目录
                        Files.createDirectories(createFilePath.getParent());
                        //创建文件
                        if ("1".equals(fileCreateTypeProp)) {
                            //重写模式
                            if (Files.exists(createFilePath)) {
                                //检查文件内容是否一致，如果完全一致则不需要再覆盖了
                                if (!new String(Files.readAllBytes(createFilePath), StandardCharsets.UTF_8)
                                        .equals(new String(baoBytesCode, StandardCharsets.UTF_8))) {
                                    //文件存在且内容不同!!!!!!!!!!替换！！！！！！
                                    Files.write(createFilePath, baoBytesCode);
                                }
                            } else {
                                //文件不存在则创建
                                if (baoBytesCode.length > 0) {
                                    Files.write(createFilePath, baoBytesCode);
                                }
                            }
                        } else if ("2".equals(fileCreateTypeProp)) {
                            //替换模式。只支持xml。对“标签”和属性匹配的进行替换（文件不存在则创建，存在则替换）
                            if (!Files.exists(createFilePath) && baoBytesCode.length > 0) {
                                Files.write(createFilePath, baoBytesCode);
                            } else {
                                //文件存在，进行匹配替换
                                replaceXmlAndWriteFile(createFilePath, new String(baoBytesCode, StandardCharsets.UTF_8));
                            }

                        } else {
                            //不重写模式（文件不存在则创建，存在则不覆盖）
                            if (!Files.exists(createFilePath) && baoBytesCode.length > 0) {
                                Files.write(createFilePath, baoBytesCode);
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException("执行失败!!!!!!!!!!!!!!!!!!!" + e.getMessage(), e);
                    }

                } else {
                    //非.ftl结尾文件直接跳过
                    return;
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("执行失败!!!!!!!!!!!!!!!!!!!" + e.getMessage(), e);
        }

    }

    private byte[] loadFtlTemplate(Map<String, Object> commonMap, Configuration cfg, Path configFilePath) throws IOException, TemplateException {
        Assert.isTrue(Files.exists(configFilePath) && !Files
                .isDirectory(configFilePath), "确保文件存在且不是文件夹!!!!!" + configFilePath.toAbsolutePath()
                .toString());
        Template template = cfg.getTemplate(configFilePath.getFileName().toString());
        ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
        Writer out = new OutputStreamWriter(bao);
        template.process(commonMap, out);
        out.flush();
        out.close();
        return bao.toByteArray();
    }

    private void replaceXmlAndWriteFile(Path oldFilePath, String newXmlStr) throws IOException, DocumentException {

        SAXReader reader = new SAXReader(false);
        // 忽略DTD，降低延迟
        reader.setEntityResolver(new IgnoreDTDEntityResolver());
        Document oldDocument = reader.read(oldFilePath.toFile());

    }
}
