package org.seckill.util;

import com.alibaba.druid.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    private static final Pattern mobile_pattern = Pattern.compile("^1[3-9]\\d{9}$");

    public static boolean isMobile(String mobile){
        if (StringUtils.isEmpty(mobile)) return false;
        Matcher m = mobile_pattern.matcher(mobile);
        return m.matches();
    }

    public static void main(String []args){
        System.out.println(isMobile("1369167042"));
    }
}
