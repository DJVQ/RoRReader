package com.example.myreadproject8.greendao.entity.rule.convert;

import com.example.myreadproject8.greendao.entity.rule.TocRule;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;

import org.greenrobot.greendao.converter.PropertyConverter;


/**
 * @author fengyue
 * @date 2021/2/8 18:28
 */
public class TocRuleConvert implements PropertyConverter<TocRule, String> {

    @Override
    public TocRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, TocRule.class);
    }

    @Override
    public String convertToDatabaseValue(TocRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}