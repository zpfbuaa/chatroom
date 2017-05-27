package cn.itcast.service;

import cn.itcast.dao.UserDao;
import cn.itcast.dao.UserDaoImple;
import cn.itcast.vo.User;

public class UserService {

	public User login(User user) {
		UserDao dao = new UserDaoImple();
		return dao.login(user);
	}
	
	
}
