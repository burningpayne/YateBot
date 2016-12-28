package YateBotFour.YateBotFour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.Timer;

import org.pircbotx.*;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

public class YateBot 
{
	static Connection TwitchConn;
	static Connection DiscordConn;
	static ArrayList<String> Bots = new ArrayList<String>();
	
    public static void main( String[] args ) throws Exception
    {
    	DiscordBot DBot = new DiscordBot();
    	
    	MultiBotManager BotManager = new MultiBotManager();
    	
    	TwitchConn = DriverManager.getConnection("jdbc:mysql://localhost/twitch?user=root&password=iMakeItRain590872&autoReconnect=true");
    	TwitchConn.setAutoCommit(true);
    	DiscordConn = DriverManager.getConnection("jdbc:mysql://localhost/discord?user=root&password=iMakeItRain590872&autoReconnect=true");
    	DiscordConn.setAutoCommit(true);
    	
    	Statement State = TwitchConn.createStatement();
    	State.execute("SELECT * FROM `channels`");
    	ResultSet Result = State.getResultSet();
    	String Channel;
    	while (Result.next())
    	{
    		Channel = Result.getString("Name");
    		Bots.add(Channel);
    		Configuration Config = new Configuration.Builder()
    				.setName("YateBot") //  Bot's nickname
    				.setLogin("YateBot")  // Bot's login name
    				.setOnJoinWhoEnabled(false)
    				.addCapHandler(new EnableCapHandler("twitch.tv/membership"))
    				.addCapHandler(new EnableCapHandler("twitch.tv/tags"))
    				.setAutoNickChange(true)  // Add numbers to nickname if nickname is taken
    				.addServer("irc.twitch.tv", 6667)  // Sets server host and port
    				.setServerPassword("oauth:wygn2aise88zu1bsaxizx4ifq02hdd")  // Sets server password
    				.addAutoJoinChannel("#" + Channel)  // Join this bot's designated channel
    				.setCapEnabled(true)  // Enable CAP features
    				.buildConfiguration();
    		TwitchBot Bot = new TwitchBot(Config);
    		BotManager.addBot(Bot);
    	}
    	
    	
    	//  Check for channel join or part requests and handle them once every minute.
		int Delay = 60000;
		final Timer Time = new Timer(Delay, null);
		Time.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent Ev)
			{
				try  // Handling joins
				{
					Statement State = TwitchConn.createStatement();
			    	State.execute("SELECT * FROM `channels`");
			    	ResultSet Result = State.getResultSet();
			    	while (Result.next())
			    	{
			    		if (Bots.contains(Result.getString("Name")))
			    			continue;
			    		String Channel = Result.getString("Name");
			    		Configuration Config = new Configuration.Builder()
			    				.setName("YateBot") //  Bot's nickname
			    				.setLogin("YateBot")  // Bot's login name
			    				.setAutoNickChange(true)  // Add numbers to nickname if nickname is taken
			    				.addServer("irc.twitch.tv", 6667)  // Sets server host and port
			    				.setServerPassword("oauth:wygn2aise88zu1bsaxizx4ifq02hdd")  // Sets server password
			    				.addAutoJoinChannel("#" + Channel)  // Join this bot's designated channel
			    				.setCapEnabled(true)  // Enable CAP features
			    				.buildConfiguration();
			    		TwitchBot Bot = new TwitchBot(Config);
			    		BotManager.addBot(Bot);
			    		//BotManager.start();
			    		//Bot.send().message("#" + Channel, "Hello!");
			    	}
			    	
			    	try  // Handling parts.
			    	{
			    		String Channel;
			    		for (int i = 0; i < BotManager.getBots().size(); i++)
			    		{
			    			Channel = BotManager.getBots().asList().get(i).getConfiguration().getAutoJoinChannels().toString().substring(2, BotManager.getBots().asList().get(i).getConfiguration().getAutoJoinChannels().toString().length()-2);
			    			Statement StateTwo = TwitchConn.createStatement();
					    	StateTwo.execute("SELECT * FROM `channels` WHERE `Name` = '" + Channel + "'");
					    	ResultSet ResultTwo = StateTwo.getResultSet();
					    	ResultTwo.next();
					    	int Delete = ResultTwo.getInt("LeaveEvent");
					    	
					    	if (Delete == 0)
					    		continue;
					    	Bots.remove(Channel);
					    	BotManager.getBots().asList().get(i).send().message("#" + Channel, "Goodbye!");
					    	BotManager.getBots().asList().get(i).send().quitServer();
					    	StateTwo.execute("DELETE FROM `channels` WHERE `Name` = '" + Channel + "'");
			    		}
			    	}
			    	catch (Exception e)
			    	{
			    		e.printStackTrace();
			    	}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}    			
		});
		Time.start();
    	
    	BotManager.start();
    }
}
