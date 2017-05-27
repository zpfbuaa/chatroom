package cn.itcast.action;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import cn.itcast.service.UserService;
import cn.itcast.utils.BaseServlet;
import cn.itcast.vo.User;

public class UserServlet extends BaseServlet {
	/**
	 * 检查session是否过期
	 * @throws IOException 
	 */
	public String check(HttpServletRequest req,HttpServletResponse resp) throws IOException{
		// 从session中获得用户的信息
		User existUser = (User) req.getSession().getAttribute("existUser");
		// 判断session中的用户是否过期
		if(existUser == null){
			// 登录的信息已经过期了!
			resp.getWriter().println("1");
		}else{
			// 登录的信息没有过期
			resp.getWriter().println("2");
		}
		return null;
	}
	
	/**
	 *  退出聊天室
	 * @throws IOException 
	 */
	public String exit(HttpServletRequest request,HttpServletResponse response) throws IOException{
		// 获得session对象
		HttpSession session = request.getSession();
		// 将session销毁.
		session.invalidate();
		// 页面转向.
		response.sendRedirect(request.getContextPath()+"/index.jsp");
		return null;
	}
	
	/**
	 * 发送聊天内容
	 * @throws IOException 
	 */
	public String sendMessage(HttpServletRequest req,HttpServletResponse resp) throws IOException{
		// 1.接收数据 。
		System.out.println("sendMessage invoke....");
		String from = req.getParameter("from"); // 发言人
		String face = req.getParameter("face"); // 表情
		String to = req.getParameter("to"); // 接收者
		String color = req.getParameter("color"); // 字体颜色
		String content = req.getParameter("content"); // 发言内容
		// 发言时间 正常情况下使用SimpleDateFormat
		String sendTime = new Date().toLocaleString(); // 发言时间
		// 获得ServletContext对象.
		ServletContext application = getServletContext();
		//  从ServletContext中获取消息
		String sourceMessage = (String) application.getAttribute("message");
		// 拼接发言的内容:xx 对 yy 说 xxx
		sourceMessage += "<font color='blue'><strong>" + from
				+ "</strong></font><font color='#CC0000'>" + face
				+ "</font>对<font color='green'>[" + to + "]</font>说："
				+ "<font color='" + color + "'>" + content + "</font>（"
				+ sendTime + "）<br>";
		// 将消息存入到application的范围
		application.setAttribute("message", sourceMessage);
		return getMessage(req, resp);
	}
	
	/**
	 * 获取消息的方法
	 * @throws IOException 
	 */
	public String getMessage(HttpServletRequest req,HttpServletResponse resp) throws IOException{
		String message = (String) getServletContext().getAttribute("message");
		if(message != null){
			resp.getWriter().println(message);
		}
		return null;
	}
	/**
	 * 踢人的功能
	 * @throws IOException 
	 */
	public String kick(HttpServletRequest req,HttpServletResponse resp) throws IOException{
		// 1.接收参数
		int id = Integer.parseInt(req.getParameter("id"));
		// 2.踢人:从userMap中将用户对应的session销毁.
		// 获得userMap集合(在线列表)
		Map<User, HttpSession> userMap = (Map<User, HttpSession>) getServletContext()
				.getAttribute("userMap");
		// 获得这个用户对应的session.如何知道是哪个用户呢? id已经传递过来.去数据库中查询.
		// 重写user的equals 和 hashCode 方法 那么只要用户的id相同就认为是同一个用户.
		User user = new User();
		user.setId(id);
		// 从map集合中获得用户的对应的session 
		HttpSession session = userMap.get(user);
		// 销毁session
		session.invalidate();
		// 重定向到页面
		resp.sendRedirect(req.getContextPath()+"/main.jsp");
		return null;
	}
	
	/**
	 * 登录的功能
	 */
	public String login(HttpServletRequest req,HttpServletResponse resp){
		// 接收数据
		Map<String, String[]> map = req.getParameterMap();
		User user = new User();
		// 封装数据
		try {
			BeanUtils.populate(user, map);
			// 调用Service层处理数据 
			UserService us = new UserService();
			User existUser = us.login(user);
			if (existUser == null) {
				// 用户登录失败
				req.setAttribute("msg", "用户名或密码错误!");
				return "/index.jsp";
			} else {
				// 用户登录成功
				// 第一个BUG的解决:第二个用户登录后将之前的session销毁!
				req.getSession().invalidate();
				
				// 第二个BUG的解决:判断用户是否已经在Map集合中,存在：已经在列表中.销毁其session.
				// 获得到ServletCOntext中存的Map集合.
				Map<User, HttpSession> userMap = (Map<User, HttpSession>) getServletContext()
						.getAttribute("userMap");
				// 判断用户是否已经在map集合中'
				if(userMap.containsKey(existUser)){
					// 说用map中有这个用户.
					HttpSession session = userMap.get(existUser);
					// 将这个session销毁.
					session.invalidate();
				}
				
				// 使用监听器:HttpSessionBandingListener作用在JavaBean上的监听器.
				req.getSession().setAttribute("existUser", existUser);
				ServletContext application = getServletContext();

				String sourceMessage = "";

				if (null != application.getAttribute("message")) {
					sourceMessage = application.getAttribute("message")
							.toString();
				}

				sourceMessage += "系统公告：<font color='gray'>"
						+ existUser.getUsername() + "走进了聊天室！</font><br>";
				application.setAttribute("message", sourceMessage);

				resp.sendRedirect(req.getContextPath() + "/main.jsp");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

}
