package com.example.myreadproject8.greendao.entity.rule.convert;

import com.example.myreadproject8.greendao.entity.rule.FindRule;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;

import org.greenrobot.greendao.converter.PropertyConverter;



/**
 * @author fengyue
 * @date 2021/2/8 18:28
 */
public class FindRuleConvert implements PropertyConverter<FindRule, String> {

    @Override
    public FindRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, FindRule.class);
    }

    @Override
    public String convertToDatabaseValue(FindRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}