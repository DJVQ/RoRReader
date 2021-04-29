package com.example.myreadproject8.greendao.entity.rule.convert;

import com.example.myreadproject8.greendao.entity.rule.ContentRule;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;

import org.greenrobot.greendao.converter.PropertyConverter;



/**
 * @author fengyue
 * @date 2021/2/8 18:27
 */
public class ContentRuleConvert implements PropertyConverter<ContentRule, String> {
    @Override
    public ContentRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, ContentRule.class);
    }

    @Override
    public String convertToDatabaseValue(ContentRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}
