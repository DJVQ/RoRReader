package com.example.myreadproject8.greendao.entity.rule.convert;

import com.example.myreadproject8.greendao.entity.rule.SearchRule;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;

import org.greenrobot.greendao.converter.PropertyConverter;



/**
 * @author fengyue
 * @date 2021/2/8 18:29
 */
public class SearchRuleConvert implements PropertyConverter<SearchRule, String> {

    @Override
    public SearchRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, SearchRule.class);
    }

    @Override
    public String convertToDatabaseValue(SearchRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}