package ${diy_model_targetPackage}.${introspectedTable.tableConfiguration.properties.module}.baseDo;

import java.io.Serializable;
<#-- 导入包，去除重复包 -->
<#list introspectedTable.allColumns as allColumns>
    <#if allColumns.fullyQualifiedJavaType.fullyQualifiedNameWithoutTypeParameters ?index_of("java.lang")!=0>
        <#list introspectedTable.allColumns as allColumns2>
            <#if allColumns2.fullyQualifiedJavaType.fullyQualifiedNameWithoutTypeParameters ?index_of("java.lang")!=0>
                <#if allColumns.fullyQualifiedJavaType.fullyQualifiedNameWithoutTypeParameters ==allColumns2.fullyQualifiedJavaType.fullyQualifiedNameWithoutTypeParameters>
                    <#if allColumns_index==allColumns2_index>
import ${allColumns.fullyQualifiedJavaType.fullyQualifiedNameWithoutTypeParameters};
                    <#else>
                        <#break >
                    </#if>
                </#if>
            </#if>
        </#list>
    </#if>
</#list>

/**
 * 表名：${introspectedTable.fullyQualifiedTable.introspectedTableName}
 * ${introspectedTable.remarks}
 *
 */
public class ${tuofengTableName?cap_first} implements Serializable {

    private static final long serialVersionUID = 1L;

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#-- 主键字段属性 -->
<#list introspectedTable.primaryKeyColumns as primaryKey>
    /** ${primaryKey.remarks} */
    private ${primaryKey.fullyQualifiedJavaType.shortNameWithoutTypeArguments} ${primaryKey.javaProperty};
</#list>
<#-- 非主键字段属性 -->
<#list introspectedTable.nonPrimaryKeyColumns as baseColumns>
    /** ${baseColumns.remarks} */
    private ${baseColumns.fullyQualifiedJavaType.shortNameWithoutTypeArguments} ${baseColumns.javaProperty};
</#list>

<#-- 主键get、set方法 -->
<#list introspectedTable.primaryKeyColumns as primaryKey>
    public ${primaryKey.fullyQualifiedJavaType.shortNameWithoutTypeArguments} get${primaryKey.javaProperty?cap_first}() {
        return ${primaryKey.javaProperty};
    }

    public void set${primaryKey.javaProperty?cap_first}(${primaryKey.fullyQualifiedJavaType.shortNameWithoutTypeArguments} ${primaryKey.javaProperty}) {
        this.${primaryKey.javaProperty} = ${primaryKey.javaProperty};
    }

    public ${tuofengTableName?cap_first} with${primaryKey.javaProperty?cap_first}(${primaryKey.fullyQualifiedJavaType.shortNameWithoutTypeArguments} ${primaryKey.javaProperty}) {
        this.${primaryKey.javaProperty} = ${primaryKey.javaProperty};
        return this;
    }

</#list>
<#-- 非主键get、set方法 -->
<#list introspectedTable.nonPrimaryKeyColumns as baseColumns>
    public ${baseColumns.fullyQualifiedJavaType.shortNameWithoutTypeArguments} get${baseColumns.javaProperty?cap_first}() {
        return ${baseColumns.javaProperty};
    }

    public void set${baseColumns.javaProperty?cap_first}(${baseColumns.fullyQualifiedJavaType.shortNameWithoutTypeArguments} ${baseColumns.javaProperty}) {
        this.${baseColumns.javaProperty} = ${baseColumns.javaProperty};
    }

    public ${tuofengTableName?cap_first} with${baseColumns.javaProperty?cap_first}(${baseColumns.fullyQualifiedJavaType.shortNameWithoutTypeArguments} ${baseColumns.javaProperty}) {
        this.${baseColumns.javaProperty} = ${baseColumns.javaProperty};
        return this;
    }

</#list>
}
