package com.example.myreadproject8.greendao.entity.rule.convert;

import com.example.myreadproject8.greendao.entity.rule.InfoRule;
import com.example.myreadproject8.util.gson.GsonExtensionsKt;

import org.greenrobot.greendao.converter.PropertyConverter;



/**
 * @author fengyue
 * @date 2021/2/8 18:28
 */
public class InfoRuleConvert implements PropertyConverter<InfoRule, String> {

    @Override
    public InfoRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, InfoRule.class);
    }

    @Override
    public String convertToDatabaseValue(InfoRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}
