package server;
import model.ParseMessage;
import model.User;
import org.slf4j.*;
import org.w3c.dom.NodeList;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GServer{
	private static ArrayList<Connection> clientsList = new ArrayList<Connection>();
	private static ArrayList<User> userList = new ArrayList<User>(); 
	private static ArrayList<String> banList = new ArrayList<String>(); 
	private static final Logger logger = LoggerFactory.getLogger(GServer.class);
	protected static Backup backup = new Backup();
	
	public static void main(String[] args) {
		logger.info("Start server!");
		
		GServer server = new GServer();
		server.execute();
		
	}
	public void execute(){
		try {
			setUsersList();
		} catch (Exception e){
			logger.error("Users not loaded: ", e);
		}
		
		ServerSocket serv = null;
		timerCheckUsers.start();
		
		try {
			serv = new ServerSocket(3456);			
			
			logger.info("Waiting...");
			while (true){			
				Socket client = serv.accept();
				logger.info("User connected.");
				
				if (!isBan(client.getInetAddress().getHostAddress())){
					Connection ct = new Connection(client);
					clientsList.add(ct);
					
					Thread t = new Thread(ct);
					t.start();
				}		
				else {
					new Connection(client).send(ParseMessage.createXML(0,0,"ban",""));
					logger.info("Ban, client disconnected.");
				}
			}			
		} catch (IOException e) {
			logger.error("Port 3456 is busy");
		}
		finally {
			try {
				if (serv!=null)
					serv.close();
			} catch (IOException e) {
				logger.error(String.valueOf(e));
			}
		}
	}
	/* check connection users */		
	private Timer timerCheckUsers = new Timer (5000, new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i<clientsList.size();){
					if (!clientsList.get(i).isAlive()){
						clientsList.remove(i);
					}
					else 
						i++; 
		    	}
		    	sendAllUserList();	
		    	
		    	setAllAliveFalse();
		    	sendAll("<?xml version='1.0'?>"
						+ "<GameServer>"
						+ "<title>ping</title>"
						+ "<from>0</from>"
						+ "</GameServer>");
			}		    		
    	});
	public void sendAll(String messege){
		for (int i = 0; i<clientsList.size();i++){
			clientsList.get(i).send(messege);
		}
	}
	public void sendAllUserList(){
		String userList = getUserList();
		for (int i = 0; i<clientsList.size();i++){
			clientsList.get(i).sendUserList(userList);
		}
	}
	public void sendTo(String target, String messege){
		logger.debug("messege:"+messege);
		try {
			findUserById(Integer.parseInt(target)).send(messege);	
		}catch (NullPointerException e){
			logger.info("user offline");
		}	
			
	}
	public String getUserList(){
		StringBuilder list = new StringBuilder();
		list.append("<?xml version='1.0'?>")
			.append("<GameServer>")
			.append("<title>userList</title>")
			.append("<from>0</from>")
			.append("<content>");
		for (int i = 0; i<clientsList.size();i++){			
			StringBuilder content = new StringBuilder();
			if (clientsList.get(i).isFree() || clientsList.get(i).getIAm()!=null && clientsList.get(i).getIAm().getLogin()!=null){
				content.append("<id>")
					.append(clientsList.get(i).getIAm().getId())
					.append("</id>")
					.append("<login>")
					.append(clientsList.get(i).getIAm().getLogin())
					.append("</login>");
			
				if (clientsList.get(i).isFree()){				
					list.append("<user>")
						.append(content)
						.append("</user>");	
				}
				else {				
					list.append("<busyUser>")
						.append(content)
						.append("</busyUser>");	
				}
			}
		}
		list.append("</content>")
			.append("</GameServer>");
		return list.toString();		
	}
	public boolean addUser(String login, String password){
		for (int i=0;i<userList.size();i++){
			if (userList.get(i).getLogin().equals(login)){
				return false;
			}
		} 		
		User newUser = new User(userList.size()+1);
		newUser.setLogin(login);
		newUser.setPassword(password);		
		userList.add(newUser);
		
		try {
			backup.addUserToXML(newUser);
		} catch (Exception e) {
			logger.error(String.valueOf(e));
		}
		
		return true;
	}
	public void setAllAliveFalse(){
		for (int i = 0; i<clientsList.size();i++){
			clientsList.get(i).setAlive(false);
		}
	}
	
	public Connection findUserById(int id){
		try {
			for (int i = 0; i<clientsList.size();i++){
				if (clientsList.get(i).getIAm().getId() == id){
					return clientsList.get(i);
				}
			}
		}
		catch (NullPointerException e) {
			logger.info("remove client");
		}
		return null;		
	}
	public Connection findUserByLogin(String target) {
		try {
			for (int i = 0; i<clientsList.size();i++){
				if (clientsList.get(i).getIAm()!=null){
					if (clientsList.get(i).getIAm().getLogin().equals(target)){
						return clientsList.get(i);
					}
				}
			}
		}
		catch (NullPointerException e) {
			logger.info("remove client");
		}
		return null;
	}
	public User isCorrectUser(String login, String password) {	
		for (int i=0;i<userList.size();i++){
			if (userList.get(i).getLogin().length()==login.length() && 
					userList.get(i).getPassword().length()==password.length()){{
						if (userList.get(i).getLogin().equals(login) &&
								userList.get(i).getPassword().equals(password)){
							if (findUserByLogin(login)!=null){
								return new User(-2);
							}
							else {
								return userList.get(i);
							}
						}
					}
			}
		}
		return new User(-1);
	}
	public void setUsersList() throws Exception {		
		NodeList users = backup.getDoc().getElementsByTagName("user");		
			for (int i = 0; i < users.getLength(); i++){
				User user = ParseMessage.parseUser(users.item(i));
				userList.add(user);
			}	
	}
	public boolean isNumber(String str){
		try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
	}
	public void setOpponents(int id1, String opponent){
		if (isNumber(opponent)){
			int id2 = Integer.parseInt(opponent);
			findUserById(id1).setOpponentId(id2);
			findUserById(id2).setOpponentId(id1);
		}
		else {
			int id2 = findUserByLogin(opponent).getIAm().getId();
			findUserById(id1).setOpponentId(id2);
			findUserById(id2).setOpponentId(id1);
		}
	}
	/* remove connection when user disconnected */
	public void removeConnection(int id){
		clientsList.remove(findUserById(id));
	}
	public void exitFromGame(int id){
		for (int i=0;i<clientsList.size();i++){
			if (clientsList.get(i).getOpponentId()==id && !clientsList.get(i).isFree()){
				clientsList.get(i).send(ParseMessage.createXML(id,clientsList.get(i).getOpponentId(),
						"game over disconnected",""+clientsList.get(i).getIAm().getPoints()));
				clientsList.get(i).getIAm().setPoints(clientsList.get(i).getIAm().getPoints()+1);
				banList.add(findUserById(id).getClientSocket().getInetAddress().getHostAddress());
				removeBanListTimer.setRepeats(false);
				removeBanListTimer.restart();
			}
		}
	}
	public boolean isBan(String ipAddress){
		for (int i=0;i<banList.size();i++){
			if (banList.get(i).equals(ipAddress))
				return true;
		}
		return false;		
	}
	private Timer removeBanListTimer = new Timer (10000,new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
			logger.info("Ban list removed.");
			banList.removeAll(banList);			
		}});
}