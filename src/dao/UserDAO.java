package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import idao.IUserDAO;
import model.User;
import util.ConnectionManager;

public class UserDAO implements IUserDAO {

	@SuppressWarnings("finally")
	@Override
	public String insert(User user) {
		
		String result = null;
		if (user == null) {
			return result;
		}
		
		String random_account = null;
		Random random = new Random();
		
		while (true) {
			
			random_account = Integer.toString(100000 + random.nextInt(499999));
			if (!isExistAccount(random_account)) {
				break;
			}
		}
		System.out.println("random_account: " + random_account); 
		user.setUser_account(random_account);
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		
		String sql = "INSERT INTO cruser(user_account, user_name, user_passwd, user_icon, user_email,"
				+ "user_tel) VALUES("
				+ "?, ?, ?, ?, ?, ?)";
		try {
			
			pStatement =  connection.prepareStatement(sql);
			pStatement.setString(1, user.getUser_account());
			pStatement.setString(2, user.getUser_name());
			pStatement.setString(3, user.getUser_passwd());
			pStatement.setString(4, user.getUser_icon());
			pStatement.setString(5, user.getUser_email());
			pStatement.setString(6, user.getUser_tel());
			
			
			pStatement.executeUpdate();
			result = random_account;
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			
			ConnectionManager.close(null, pStatement, connection);
			return result;
		}
		
	}
	
	@SuppressWarnings("finally")
	private boolean isExistAccount(String random_account) {
		
		boolean result = true;
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		
		String sql = "SELECT user_id FROM cruser WHERE user_account = ?";
		
		try {
			pStatement = connection.prepareStatement(sql);
			pStatement.setString(1, random_account);
			
			if (!pStatement.executeQuery().next()) {
				result = false;
			}
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			
			ConnectionManager.close(null, pStatement, connection);
			return result;
		}
	}

	@SuppressWarnings({ "finally" })
	@Override
	public boolean delete(String user_account) {
		
		boolean result = false;
		if (user_account == null) {
			return result;
		}
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		
		String sql1 = " DELETE FROM friends WHERE friend_account = ? OR user_account = ?";
		String sql2 = " DELETE FROM crgroup WHERE user_account = ?";
		String sql3 = " DELETE FROM messagecache WHERE from_account = ? OR to_account = ?";
		String sql4 = " DELETE FROM cruser WHERE user_account = ?";
		
		try {
			connection.setAutoCommit(false);
			
			pStatement = connection.prepareStatement(sql1);
			pStatement.setString(1, user_account);
			pStatement.setString(2, user_account);
			pStatement.executeUpdate();
			pStatement.close();
			
			pStatement = connection.prepareStatement(sql2);
			pStatement.setString(1, user_account);
			pStatement.executeUpdate();
			pStatement.close();
			
			pStatement = connection.prepareStatement(sql3);
			pStatement.setString(1, user_account);
			pStatement.setString(2, user_account);
			pStatement.executeUpdate();
			pStatement.close();
			
			pStatement = connection.prepareStatement(sql4);
			pStatement.setString(1, user_account);
			pStatement.executeUpdate();
			
			connection.commit();
			result = true;
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			
			ConnectionManager.close(null, pStatement, connection);
			return result;
		}

	}

	@SuppressWarnings("finally")
	@Override
	public boolean update(User user) {
		
		boolean result = false;
		if (user == null) {
			return result;
		}
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		
		String sql = "UPDATE cruser SET user_passwd = ?, user_name = ?, user_icon = ?,"
				+ "user_tel = ?, user_email = ? WHERE user_account = ?";
		
		try {
			pStatement = connection.prepareStatement(sql);
			pStatement.setString(1, user.getUser_passwd());
			pStatement.setString(2, user.getUser_name());
			pStatement.setString(3, user.getUser_icon());
			pStatement.setString(4, user.getUser_tel());
			pStatement.setString(5, user.getUser_email());
			pStatement.setString(6, user.getUser_account());
			
			pStatement.executeUpdate();
			result = true;
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			
			ConnectionManager.close(null, pStatement, connection);
			return result;
			
		}
	}

	@SuppressWarnings("finally")
	@Override
	public boolean verify(String user_lognIn, String user_passwd) {
		
		boolean result = false;
		if (user_lognIn == null || user_passwd == null) {
			return result;
		}
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		String condition = null;
		StringBuilder sql = new StringBuilder("SELECT user_passwd FROM cruser WHERE ");
		
		Pattern accountPattern = Pattern.compile("[0-9a-zA-Z]{6}");
		Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9-_]+@[a-zA-Z0-9-_]+(\\.[a-z]{2,3}){1,2}$");
		Pattern telPattern = Pattern.compile("^(13[4,5,6,7,8,9]|15[0,8,9,1,7]|188|187)\\d{8}$");
		
		if (accountPattern.matcher(user_lognIn).matches()) {
			condition = " user_account ";
		}
		else if (emailPattern.matcher(user_lognIn).matches()) {
			condition = " user_email ";
		}
		else if (telPattern.matcher(user_lognIn).matches()) {
			condition = " user_tel ";
		}
		sql.append(condition);
		sql.append(" = ?");
		
		try {
			pStatement = connection.prepareStatement(sql.toString());
			pStatement.setString(1, user_lognIn);
			
			resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				if (user_passwd.equals(resultSet.getString("user_passwd"))) {
					result = true;
				}
			}
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			 
			ConnectionManager.close(resultSet, pStatement, connection);
			return result;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public List<User> searchUserByName(String user_name) {
		
		List<User> result = null;
		if (user_name == null) {
			return result;
		}
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		result = new ArrayList<>();
		User user = null;
		
		String sql = "SELECT * FROM cruser WHERE user_name = ?";
		
		try {
			pStatement = connection.prepareStatement(sql);
			pStatement.setString(1, user_name);
			
			resultSet = pStatement.executeQuery();
			
			while (resultSet.next()) {
				user = new User();
				user.setUser_account(resultSet.getString("user_account"));
				user.setUser_id(resultSet.getInt("user_id"));
				user.setUser_name(resultSet.getString("user_name"));
				user.setUser_passwd(resultSet.getString("user_passwd"));
				user.setUser_icon(resultSet.getString("user_icon"));
				
				result.add(user);
				
			}
			
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			
			ConnectionManager.close(resultSet, pStatement, connection);
			return result;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public User searchUserByCondition(String demand) {
		
		User result = null;
		if (demand == null) {
			return result;
		}
		
		Connection connection = ConnectionManager.getInstance().getConnection();
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		String condition = null;
		
		StringBuilder sql = new StringBuilder("SELECT * FROM cruser WHERE ");
		
		Pattern accountPattern = Pattern.compile("[0-9a-zA-Z]{6}");
		Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9-_]+@[a-zA-Z0-9-_]+(\\.[a-z]{2,3}){1,2}$");
		Pattern telPattern = Pattern.compile("^(13[4,5,6,7,8,9]|15[0,8,9,1,7]|188|187)\\d{8}$");
		
		if (accountPattern.matcher(demand).matches()) {
			condition = " user_account ";
		}
		else if (emailPattern.matcher(demand).matches()) {
			condition = " user_email ";
		}
		else if (telPattern.matcher(demand).matches()) {
			condition = " user_tel ";
		}
		sql.append(condition);
		sql.append(" = ?");
		
		try {
			
			pStatement = connection.prepareStatement(sql.toString());
			pStatement.setString(1, demand);
			resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				result = new User();
				result.setUser_account(resultSet.getString("user_account"));
				result.setUser_id(resultSet.getInt("user_id"));
				result.setUser_name(resultSet.getString("user_name"));
				result.setUser_passwd(resultSet.getString("user_passwd"));
				result.setUser_icon(resultSet.getString("user_icon"));
			}
			
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			
			ConnectionManager.close(resultSet, pStatement, connection);
			return result;
		}

	}

}
