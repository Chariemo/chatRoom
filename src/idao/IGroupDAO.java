package idao;

import java.util.List;

import model.Group;
import model.User;

public interface IGroupDAO {
	
	public boolean creatGroup(Group group, String user_account);
	
	public boolean deleteGroup(int group_id, String user_account);
	
	public boolean updateGroup(Group group, String user_account);
	
	public User searchUserInGroup(int group_id, String user_account, String find_account);
	
	public List<User> searchAllUsersByGroup(int group_id, String user_account);
	
}
