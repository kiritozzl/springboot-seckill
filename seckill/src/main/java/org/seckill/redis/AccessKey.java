package org.seckill.redis;

public class AccessKey extends BasePrefix{
    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static AccessKey getAccessKey = new AccessKey(30, "gs");

    public static AccessKey getNewKey(int expireSeconds){
        return new AccessKey(expireSeconds,"gs");
    }
}
