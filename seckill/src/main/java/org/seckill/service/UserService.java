package org.seckill.service;

import com.alibaba.druid.util.StringUtils;
import org.seckill.bean.User;
import org.seckill.exception.GlobalException;
import org.seckill.mapper.UserMapper;
import org.seckill.redis.RedisService;
import org.seckill.redis.UserKey;
import org.seckill.result.CodeMsg;
import org.seckill.util.MD5Util;
import org.seckill.util.UUIDUtil;
import org.seckill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User getById(long id){
        User user = redisService.get(UserKey.getById,""+id,User.class);
        if (user != null) return user;
        user = userMapper.getById(id);
        if (user != null){
            redisService.set(UserKey.getById,""+id,user);
        }
        return user;
    }

    public boolean updatePassword(String token, long id, String formPass){
        User user = getById(id);
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        User nu = new User();
        nu.setId(id);
        nu.setPassWord(MD5Util.inputPassToDbPass(formPass,user.getSalt()));
        userMapper.update(nu);
        //更新缓存：先删除再插入
        redisService.delete(UserKey.getById,""+id);
        redisService.set(UserKey.token,token,nu);
        return true;
    }

    public String login(HttpServletResponse response, LoginVo loginVo){
        if (loginVo == null) throw new GlobalException(CodeMsg.SERVER_ERROR);
        String mobile = loginVo.getMobile();
        String passwd = loginVo.getPassword();
        User user = getById(Long.parseLong(mobile));
        if (user == null) throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        String dbSalt = user.getSalt();
        String dbPass = user.getPassWord();//MD5Util.inputPassToDbPass(user.getPassWord(), dbSalt);
//        logger.error("db_passwd:"+user.getPassWord()+" formpass:"+MD5Util.inputPassToFormPass(user.getPassWord()) + "dbpass:"+
//                dbPass+" dbsalt:"+dbSalt);
        String clapass = MD5Util.formPassToDBPass(passwd,dbSalt);
//        logger.error("passwd:"+passwd+"clapass:"+clapass);
        if (!clapass.equals(dbPass)) throw new GlobalException(CodeMsg.PASSWORD_ERROR);

        String token = UUIDUtil.uuid();
        addCookie(response,token,user);
        return token;
    }

    /**
     * 将token做为key，用户信息做为value 存入redis模拟session
     * 同时将token存入cookie，保存登录状态
     */
    public void addCookie(HttpServletResponse response, String token, User user){
        redisService.set(UserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");//设置为网站根目录
        response.addCookie(cookie);
    }

    /**
     * 根据token获取用户信息
     */
    public User getByTooken(HttpServletResponse response, String token){
        if (StringUtils.isEmpty(token)) return null;
        User user = redisService.get(UserKey.token,token,User.class);
        if (user != null){
            addCookie(response,token,user);
        }
        return user;
    }
}
