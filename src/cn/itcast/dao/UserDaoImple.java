package cn.itcast.dao;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import cn.itcast.utils.JDBCUtils;
import cn.itcast.vo.User;

public class UserDaoImple implements UserDao {

	public User login(User user) {
		QueryRunner queryRunner = new QueryRunner(JDBCUtils.getDataSource());
		String sql = "select * from user where username = ? and password = ?";
		User existUser;
		try {
			existUser = queryRunner.query(sql, new BeanHandler<User>(User.class), user.getUsername(),user.getPassword());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("用户登录失败!");
		}
		return existUser;
	}

}
