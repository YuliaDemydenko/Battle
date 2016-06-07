package server;
import model.ParseMessage;
import model.User;
import org.slf4j.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class Connection extends GServer implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;	
	private boolean free=false;
	private boolean isAlive=true;
	
	private User iAm;	
	private int opponentId;
	
	public Connection(Socket clientSocket){
		this.clientSocket=clientSocket;
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);		
			in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
		}
	}
	
	public int getOpponentId() {
		return opponentId;
	}
	public void setOpponentId(int opponentId) {
		this.opponentId=opponentId;
	}
	public User getIAm() {
		return iAm;
	}
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	public void send (String messege){		
		ParseMessage parsXML = new ParseMessage(messege);
		if (parsXML.getTitle().equals("challenge") && ( parsXML.getTo().equals(""+iAm.getId()) || parsXML.getTo().equals(iAm.getLogin()) )  && !isFree()){
			sendTo( parsXML.getFrom() ,"<?xml version='1.0'?><GameServer><title>busy</title><from>0</from></GameServer>"); 
			return;
		}
		out.println(messege);
	}
	public void setAlive(boolean b){
		isAlive=b;
	}
	public boolean isAlive(){
		return isAlive;
	}
	public boolean isFree(){
		return free;
	}
	public void sendUserList(String userList){
		send(userList);
	}
	
	@Override
	public void run() {	
		try {
			String mes;
			ParseMessage xmlDoc;
			while ((mes=in.readLine())!=null){
				xmlDoc=new ParseMessage(mes);
				String title = xmlDoc.getTitle();
				String toLogin = xmlDoc.getTo();
				int toId = -3;
				
				if (isNumber(toLogin))
					toId = Integer.parseInt(toLogin);
				
				if (toId==0){
					if (title.equals("ping")){
						setAlive(true);
					}
					else if (title.equals("update user")){
						iAm.setCountGamesWon(Integer.parseInt(xmlDoc.getCountGamesWon()));
						iAm.setCountLostGames(Integer.parseInt(xmlDoc.getCountLostGames()));
						iAm.setPoints(Integer.parseInt(xmlDoc.getPoints()));
						try {
							backup.updateUser(iAm);
						} catch (Exception e) {
							logger.error("updateUser: "+e);
						}
					}
					else if (title.equals("setFree")){
						boolean b=true;
						if (xmlDoc.getContent().equals("false"))
							b=false;
						free=b;
					}
					else if (title.equals("login")){			
						iAm = isCorrectUser(xmlDoc.getLogin(),xmlDoc.getPassword());
						
						if ( iAm.getId()>-1 ){
							free=true;
							String u = backup.createUserNode(iAm);
							send(ParseMessage.createXML(0,iAm.getId(),"login",u));
							sendUserList(getUserList());	
						}
						else if (iAm.getId()==-1 ) {
							send(ParseMessage.createXML(0,iAm.getId(),"login","false"));
						}
						else {
							send(ParseMessage.createXML(0,iAm.getId(),"login","use"));
						}
					}
					else if (title.equals("registration")){
						send(ParseMessage.createXML(0,0,"registration",""+addUser(xmlDoc.getLogin(),xmlDoc.getPassword())));
					}
					else if (title.equals("opponentId")){
						setOpponents(iAm.getId(),xmlDoc.getContent());
					}
				}				
				else {
					sendTo(toLogin, mes);
				}
			}
		} catch (SocketException e) {
			
			try {
				int myId = iAm.getId();
				logger.info("User "+myId+" disconnected");				
				if (!isFree()){
					exitFromGame(myId);
				}
				removeConnection(myId);
			}
			catch (NullPointerException event){
			}	
		} catch (IOException e) {
			logger.error(String.valueOf(e));
		}
		finally{
			try {
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e) {
				logger.error(String.valueOf(e));
			}
		}
	}
}
