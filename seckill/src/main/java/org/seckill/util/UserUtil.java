package org.seckill.util;

import org.seckill.bean.User;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserUtil {
    public static void main(String[] args) throws Exception {
        int user_count = 5000;
        List<User> users = new ArrayList<>();
        for(int i = 0; i < user_count; i++){
            User user = new User();
            user.setId(13000000000L+i);
            user.setLoginCount(1);
            user.setNickName("user"+i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c");
            user.setPassWord(MD5Util.inputPassToDbPass("123456", user.getSalt()));
            users.add(user);
        }
        File file = new File("D:/users.txt");
        if (file.exists()) file.delete();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for(int i = 0; i < user_count; i++){
            User user = users.get(i);
            String row = user.getId() + "\r\n";
            raf.seek(raf.length());
            raf.write(row.getBytes());
            System.out.println("write to file "+user.getId());
        }
        raf.close();

        System.out.println("over");
//        Connection connection = DBUtil.getConnection();
//        String sql = "insert into sk_user (login_count, nickname, register_date, salt, password, id) values (?,?,?,?,?,?)";
//        PreparedStatement statement = connection.prepareStatement(sql);
//        for(int i = 0; i < user_count; i++){
//            User user = users.get(i);
//            statement.setInt(1,user.getLoginCount());
//            statement.setString(2,user.getNickName());
//            statement.setTimestamp(3,new Timestamp(user.getRegisterDate().getTime()));
//            statement.setString(4,user.getSalt());
//            statement.setString(5,user.getPassWord());
//            statement.setLong(6,user.getId());
//            statement.addBatch();
//        }
//        statement.executeBatch();
//        statement.close();
//        connection.close();
//        System.out.println("insert to db");
    }
}
