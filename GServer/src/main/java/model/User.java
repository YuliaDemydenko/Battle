package model;

public class User {
	private int id;
	private String login;
	private String password;
	private int countGamesWon = 0;
	private int countLostGames = 0;
	private int points = 10;
	
	public User(int id ){
		this.id=id;
	}
	public User(int id, String login, String password, int countGamesWon, int countLostGames, int points ){
		this.id=id;
		this.login=login;
		this.password=password;
		this.countGamesWon=countGamesWon;
		this.countLostGames=countLostGames;
		this.points=points;
	}
	public int getId() {
		return id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getCountGamesWon() {
		return countGamesWon;
	}
	public void setCountGamesWon(int countGamesWon) {
		this.countGamesWon = countGamesWon;
	}
	public int getCountLostGames() {
		return countLostGames;
	}
	public void setCountLostGames(int countLostGames) {
		this.countLostGames = countLostGames;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
}
