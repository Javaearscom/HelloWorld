package com.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.ManagerDAO;
import com.dao.UserInfoDAO;
import com.entity.UserInfo;
import com.match.CheckDAO;
import com.sun.corba.se.spi.activation.Repository;

public class ServletManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ServletConfig config = null;
	private ManagerDAO manager_dao = new ManagerDAO();
	private CheckDAO check_dao = new CheckDAO();
	private UserInfo mUser = null;
	private UserInfoDAO udao = new UserInfoDAO();

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//request.setCharacterEncoding("gb2312");			//设置请求编码格式
		//response.setCharacterEncoding("gb2312");		//设置响应编号格式
		request.setCharacterEncoding("gbk");
		response.setCharacterEncoding("gbk");
		response.setContentType("text/html");			//设置响应文本类型
		PrintWriter out = response.getWriter();			//获得响应输出流
		String action = request.getParameter("action");	//获得action参数
		//System.out.println("action参数是"+action);
		if ("add".equals(action.toLowerCase())) {		//添加版块
			Integer sParentId = Integer
				.parseInt(request.getParameter("select"));//获得父版块编号
			Integer sType = Integer
				.parseInt(request.getParameter("sType"));//获得版块类型
			
			String sName = request.getParameter("sName").trim();//获得版块名称
			//userName.getBytes("ISO-8859-1"), "gb2312"
			//String eName = request.getParameter("sName").trim();
			
			//System.out.println("版块名称"+sName);
			
			//String sName = new String(eName.getBytes("gbk"),"gbk");
			
			
			//System.out.println("版块名称"+sName);
			String uName = request.getParameter("uName").trim();//获得版主用户名
			if (sType == 0) {							//重要热带病研究版块
				sParentId = 0;							//设置其父版块编号为0
			} else if (sType == 1) {					//重要热带病研究子版块
				if (sParentId == -1) {					//如果父版块编号为-1
					out.print("<script>" + "alert('请选择重要热带病研究所属父版块');"
							+ "window.history.back();" + "</script>");
					return;								//弹出对话框提示
				}
			}
			if (check_dao.checkSectionName(sName)) {	//验证版块名称
				if(check_dao.checkUserNameIsExist(uName)) {//验证版主用户名
					Boolean flag = manager_dao.
						addSection(sParentId, sName,uName);//执行新增版块
					if (!flag) {						//添加失败
						out.print("<script>" + "alert('添加失败');"
						+ "window.history.back();" + "</script>");
					} else {							//添加成功
						response.sendRedirect("manager/addSection.jsp");
					}
				}else {
					out.print("<script>" + "alert('该用户不存在，不能指定为版主');"
						+ "window.history.back();" + "</script>");
				}
			} else {
				out.print("<script>" + "alert('重要热带病研究版块名称长度为3-20');"
						+ "window.history.back();" + "</script>");
			}
	
		} else if ("edit".equals(action.toLowerCase())) {
			Integer sId = Integer.
						parseInt(request.getParameter("sid"));	//获得版块编号
			String sName = request.getParameter("sName");		//获得版块名称
			String uName = request.getParameter("uName").trim();//获得版主用户名
			out.println(sName);
			if (check_dao.checkSectionName(sName)) {			//验证版块名
				if(check_dao.checkUserNameIsExist(uName)) {		//验证版主用户名
					Boolean flag = manager_dao.
						updateSectionInfoById(sId, sName,uName);//执行更新
					if (!flag) {								//修改失败
						out.print("<script>" + "alert('修改失败');"
								+ "window.history.back();" + "</script>");
					} else {									//修改成功
						response.sendRedirect("manager/managerSection.jsp");
					}
				}else{
					out.print("<script>" + "alert('该用户不存在，不能指定为版主');"
							+ "window.history.back();" + "</script>");
				}
			} else {
				out.print("<script>" + "alert('重要热带病研究版块名称长度为3-20');"
						+ "window.history.back();" + "</script>");
			}
		
			} else if ("del".equals(action.toLowerCase())) {
				Integer sid = Integer.parseInt(request.getParameter("sid"));
				if (!manager_dao.isHaveChildNode(sid)) {// 如果版块不含有子节点
					Boolean flag = manager_dao.delSectionInfo(sid);
					if (!flag) {// 删除失败
						out.print("<script>" + "alert(\'删除失败\');" + "</script>");
					} else {// 删除成功
						response.sendRedirect("manager/managerSection.jsp");
					}
				} else {
					out.print("<script>" + "alert(\'该版块含有子版块，不能删除\');"
							+ "window.history.back();" + "</script>");
				}
			} else if ("move".equals(action.toLowerCase())) {
			Integer moveType = Integer.parseInt(request
					.getParameter("moveType"));
			Integer sourceSid = Integer
					.parseInt(request.getParameter("source"));
			Integer targetSid = Integer
					.parseInt(request.getParameter("target"));
			if (moveType == 0) {
				Boolean flag = manager_dao.moveToChildSectionInfoById(
						sourceSid, targetSid);
				if (!flag) {// 移动失败
					out.print("<script>" + "alert('移动失败\\n不能把自身作为目标版块移动');"
							+ "window.history.back();" + "</script>");
				} else {// 移动成功
					response.sendRedirect("manager/welcome.htm");
				}
			}
			if (moveType == 1) {
				Boolean flag = manager_dao.moveToRootSectionInfoById(sourceSid);
				if (!flag) {// 移动失败
					out.print("<script>" + "alert('移动失败');"
							+ "window.history.back();" + "</script>");
				} else {// 移动成功
					response.sendRedirect("manager/welcome.htm");
				}
			}

		} else if ("login".equals(action.toLowerCase())) {
			String userName = request.getParameter("uName");
			String passWord = request.getParameter("uPass");
			
			if (udao.checkLogin(userName, passWord)) {
				mUser = udao.getUserInfo(userName);
				HttpSession session = request.getSession();// 用户登录会话开始
				session.setAttribute("mUsers", mUser);
				response.sendRedirect("manager/index.jsp");
			}else{
				out.print("<script>" + "alert('密码错误');"
						+ "window.history.back();" + "</script>");
			}
			
		}else if ("exit".equals(action.toLowerCase())) {
			HttpSession session = request.getSession();// 获得会话
			session.invalidate();//关闭会话
			response.sendRedirect("index.jsp");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//request.setCharacterEncoding("ISO-8859-1");
		//response.setCharacterEncoding("gbk");
		//response.setContentType("text/html");
		this.doGet(request, response);
	}

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}

}
