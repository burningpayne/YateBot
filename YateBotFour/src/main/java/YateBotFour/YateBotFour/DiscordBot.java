package YateBotFour.YateBotFour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import YateBotFour.YateBotFour.TwitchBot.Page;
import YateBotFour.YateBotFour.TwitchBot.StrawID;
import YateBotFour.YateBotFour.TwitchBot.StrawResults;
import YateBotFour.YateBotFour.TwitchBot.Strawpoll;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.AudioPlayerEvent;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackSkipEvent;
import sx.blah.discord.util.audio.providers.FileProvider;
import sx.blah.discord.util.Image;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;



public class DiscordBot extends YateBot
{
	IDiscordClient Bot;
	final private String KEY = "!";
	HashMap<String, String> StrawpollLink = new HashMap<String, String>();
	HashMap<String, String> StrawpollTitle = new HashMap<String, String>();
	HashMap<IGuild, IChannel> StreamChannel = new HashMap<IGuild, IChannel>();
	ArrayList<AudioInputStream> Tracks = new ArrayList<AudioInputStream>();
	ArrayList<String> StreamsLive = new ArrayList<String>();
	ArrayList<String> Rule34 = new ArrayList<String>();
	ArrayList<String> SongQueue = new ArrayList<String>();
	ArrayList<Integer> RolledDice = new ArrayList<Integer>();
	String[] EightBall = {"It is certain.", "It is decidedly so.", "Yes, definitely.", "You may rely on it.", "As I see it, yes.", "Most likely.", "Outlook good.", "Yes.", "Signs point to yes.", "Reply hazy, try again.", "Ask again later.", "Better not tell you now.", "Cannot predict now.", "Concentrate and ask again.", "Don't count on it.", "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful."};
	int MusicIndex = 0;
	boolean FirstRun = true;
	boolean MusicDownloading = false;
	IChannel TestChannel;
	IUser Yatekko;
	IUser Krystal;
	Connection Conn = null;
	private final String DiscordDB = "jdbc:mysql://localhost/discord?user=root&password=iMakeItRain590872&autoReconnect=true";
	private final String TwitchDB = "jdbc:mysql://localhost/twitch?user=root&password=iMakeItRain590872&autoReconnect=true";
	
	
	public DiscordBot() throws Exception
	{
		Bot = getClient("MjM5MzAyMzk5NzI1MDEwOTQ0.Cv1QFg.vm_gyFUywginFzu6Cxw-AfgcbZU");
		Bot.getDispatcher().registerListener(this);
		
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			Conn = DriverManager.getConnection(DiscordDB);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		
		// Set polls
		try
		{
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `polls`");
			ResultSet Result = State.getResultSet();
			while (Result.next())
			{
				StrawpollTitle.put(Result.getString(1), Result.getString(2));
				StrawpollLink.put(Result.getString(1), Result.getString(3));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		// Set streams live
		try
		{	
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `StreamsLive` WHERE 1");
			ResultSet Result = State.getResultSet();
			while (Result.next())
				StreamsLive.add(Result.getString(1));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public IDiscordClient getClient(String token) throws DiscordException
    {
    	return new ClientBuilder().withToken(token).login();
    }
	
	@EventSubscriber
	public void VoiceDisconnect(VoiceDisconnectedEvent Event)
	{
		MusicDownloading = false;
		return;
	}
	
	@EventSubscriber
	public void onReady(ReadyEvent Event) throws Exception
	{
		List<IGuild> Guilds = Bot.getGuilds();
		Status BotStatus = Status.game("discord.yatebot.yatekko.com");
		Bot.changeStatus(BotStatus);
		File AvatarFile = new File("YateBot.png");
		Bot.changeAvatar(Image.forFile(AvatarFile));
		Yatekko = getUser(getGuild("YateBot Testing"), "<@113423696147808256>");
		Krystal = getUser(getGuild("Berry Party"), "<@124576504305680386>");
		TestChannel = getGuild("YateBot Testing").getChannelByID("239300602683719681");
		//MusicFileCleanup();
		
		
		for (int i = 0; i < Guilds.size(); i++)
		{
			Statement State = Conn.createStatement();
			try  // Commands
			{
				State.execute("SELECT * FROM `" + Guilds.get(i).getName() + "`");
			}
			catch (SQLSyntaxErrorException e)
			{
				State.execute("CREATE TABLE `discord`.`" + Guilds.get(i).getName() + "` ( `Name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Command` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Mod` BOOLEAN NOT NULL , PRIMARY KEY (`Name`)) ENGINE = InnoDB;");
			}
			try  // Streams
			{
				State.execute("SELECT * FROM `" + Guilds.get(i).getName() + "streams`");
			}
			catch (SQLSyntaxErrorException e)
			{
				State.execute("CREATE TABLE `discord`.`" + Guilds.get(i).getName() + "streams` ( `UserID` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `URL` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , PRIMARY KEY (`UserID`)) ENGINE = InnoDB;");
			}
		}
	}
	
	@EventSubscriber
	public void JoinGuild(GuildCreateEvent Event)
	{
		try
		{
			Statement State = Conn.createStatement();
			try  // Commands
			{
				State.execute("SELECT * FROM `" + Event.getGuild().getName() + "`");
			}
			catch (SQLSyntaxErrorException e)
			{
				State.execute("CREATE TABLE `discord`.`" + Event.getGuild().getName() + "` ( `Name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Command` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Mod` BOOLEAN NOT NULL , PRIMARY KEY (`Name`)) ENGINE = InnoDB;");
			}
			try  // Streams
			{
				State.execute("SELECT * FROM `" + Event.getGuild().getName() + "streams`");
			}
			catch (SQLSyntaxErrorException e)
			{
				State.execute("CREATE TABLE `discord`.`" + Event.getGuild().getName() + "streams` ( `UserID` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `URL` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , PRIMARY KEY (`UserID`)) ENGINE = InnoDB;");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `botnickname` WHERE `Guild` = '" + Event.getGuild().getID() + "'");
			ResultSet Result2 = State.getResultSet();
			Result2.next();
			String Name = Result2.getString("Nickname");
			Event.getGuild().setUserNickname(Bot.getOurUser(), Name);
		}
		catch (SQLException e)
		{
			
		}
		catch (DiscordException | RateLimitException | MissingPermissionsException e)
		{
			e.printStackTrace();
		}
	}
	
	@EventSubscriber
	public void TrackFinish(TrackFinishEvent Event)
	{
		SongQueue.remove(0);
		return;
	}
	
	@EventSubscriber
	public void NewUser(UserJoinEvent Event) throws Exception
	{
		
		IChannel GeneralChan = Event.getGuild().getChannelByID(Event.getGuild().getID());
		if (Event.getGuild().getID().equals("152163383087464449"))
		{
			if (Event.getUser().getID().equals("140693846731259904"))
				sendMessage(Krystal.getOrCreatePMChannel(), "KRYSTAL ZEKKS IS HERE DO SHIT");
			sendMessage(GeneralChan, ":two_hearts: <:mangohLove:255551947459919872> " + Event.getUser().mention(false) + ", Welcome to the Berry Party! Make sure to review <#171161586449252355> and let's have some fun! <:mangohLove:255551947459919872> :two_hearts:");
			return;
		}
		sendMessage(GeneralChan, "Everyone welcome " + Event.getUser().mention(false) + " to " + Event.getGuild().getName() + "!");
		return;
	}
	
	@EventSubscriber
	public void StatusChanged(StatusChangeEvent Event) throws Exception
	{
		if (Event.getNewStatus().getType().toString().equals("NONE"))
		{
			if (StreamsLive.contains(Event.getUser().mention(false))) // Stream has gone offline.
			{
				Statement State = Conn.createStatement();
				State.execute("DELETE FROM `streamslive` WHERE `Username` = '" + Event.getUser().mention(false) + "'");
				StreamsLive.remove(Event.getUser().mention(false));	
				return;
			}
			return;
		}
		if (Event.getNewStatus().getType().toString().equals("STREAM")) // Stream went online or changed title / game
		{
			if (StreamsLive.contains(Event.getUser().mention(false))) // Stream was already online and changed title / game
				return;
			
			Optional<String> URL = Event.getNewStatus().getUrl(); // Stream has gone online.
			List<IGuild> Guilds = Bot.getGuilds();
			ArrayList<IGuild> GuildsFoundIn = new ArrayList<IGuild>();
			HashMap<IGuild, IUser> GuildOwners = new HashMap<IGuild, IUser>();
			
			StreamsLive.add(Event.getUser().mention(false));
			Statement State = Conn.createStatement();
			State.execute("INSERT INTO `streamslive` (`Username`) VALUES ('" + Event.getUser().mention(false) + "')");
			
			for (int i = 0; i < Guilds.size(); i++)
			{
				try
				{
					State.execute("SELECT * FROM `" + Guilds.get(i).getName() + "streams` WHERE 1");
					ResultSet Result = State.getResultSet();
					while (Result.next())
					{
						if (Result.getString("UserID").equals(Event.getUser().mention(false)))
							GuildsFoundIn.add(Guilds.get(i));
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					continue;
				}
			}
			
			State.execute("SELECT * FROM `channels`");
			ResultSet Result2 = State.getResultSet();
			while (Result2.next())
			{
				IGuild TempGuild = getGuild(Result2.getString(1));
				IUser TempUser = getUser(TempGuild, Result2.getString(2));
				GuildOwners.put(TempGuild, TempUser);
			}
			
			
			int Column = 0;
			for (int i = 0; i < GuildsFoundIn.size(); i++)
			{
				if (GuildOwners.containsKey(GuildsFoundIn.get(i)))  // Determine table name by whether Event.getUser() is equal to Guild Owner in HashMap
				{
					if (GuildOwners.get(GuildsFoundIn.get(i)).equals(Event.getUser()))
						Column = 3;
					else
						Column = 4;
				}
				else
					Column = 4;
				
				State.execute("SELECT * FROM `channels` WHERE 1");
				ResultSet Result = State.getResultSet();
				while (Result.next())
				{
					if (Result.getString(1).equals(GuildsFoundIn.get(i).getName()))
					{
						try
						{
							IChannel Chan = getChannel(GuildsFoundIn.get(i), Result.getString(Column));
							String Game = "";
							try 
							{
								String Username = URL.get().substring(URL.get().lastIndexOf("/")+1);
								URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + Username);
								URLConnection Connect = Strim.openConnection();
								Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
								InputStreamReader IReader = new InputStreamReader(Connect.getInputStream());
								Page Items = new Gson().fromJson(IReader, Page.class);
								Game = Items.game;
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							if (Column == 4 && Chan.getGuild().getID().equals("152163383087464449"))
								sendMessage(Chan, "**" + Event.getUser().getName() + "** has started streaming **" + Game + "**!\n        *" + Event.getNewStatus().getStatusMessage() + "*\nGo check them out!  ->  " + URL.get());
							else
								sendMessage(Chan, "@here **" + Event.getUser().getName() + "** has started streaming **" + Game + "**!\n        *" + Event.getNewStatus().getStatusMessage() + "*\nGo check them out!  ->  " + URL.get());
						}
						catch (Exception e)
						{
							System.out.println("\nError.\n");
							break;
						}
					}
				}
			}
			return;
		}
		return;
	}
	
	@EventSubscriber
	public void MessageReceived(MessageReceivedEvent Event) throws RateLimitException, DiscordException
	{
		IMessage Mess = Event.getMessage();
		String Message = Mess.getContent();
		String Command = Message.toLowerCase();
		String[] Args = null;
		IChannel Channel = Mess.getChannel();
		String User = Mess.getAuthor().getName();
		
		if (Event.getMessage().getChannel().isPrivate())
		{
			sendMessage(Yatekko.getOrCreatePMChannel(), Event.getMessage().getAuthor().mention(false) + ":  " + Event.getMessage().getContent());
			sendMessage(Channel, "Message sent.");
			return;
		}
		
		String Guild = Mess.getGuild().getName();	
		
		if (Message.equals("(╯°□°）╯︵ ┻━┻"))
		{
			sendMessage(Channel, "​┬─┬﻿ ノ( ゜-゜ノ)\nCalm down bruh.");
			return;
		}
		
		try
		{
			if (!Message.startsWith(KEY))
				return;
			
			if (Message.contains(" "))
			{
				Args = Message.substring(Message.indexOf(" ") + 1).split(" ");
				Command = Message.split(" ")[0].toLowerCase();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			if (Command.equalsIgnoreCase("!test") && Event.getMessage().getAuthor().equals(Yatekko))
			{	
				Message = Message.replaceAll("\\s+", "");
				sendMessage(Channel, Message);
				
				return;
			}
			
			if (Command.equalsIgnoreCase("!leave") && Event.getMessage().getAuthor().equals(Yatekko))
			{
				IVoiceChannel Voice = null;
				try
				{
					Voice = this.Bot.getConnectedVoiceChannels().get(0);
				}
				catch (IndexOutOfBoundsException e)
				{
					sendMessage(Channel, "I am not connected to a voice channel.");
					return;
				}
				Voice.leave();
				return;
			}
			
			if (Command.equalsIgnoreCase("!poll"))
			{
				try
				{
					try
					{
						if (Args.equals(null))
							return;
					}
					catch (NullPointerException e)
					{
						if (StrawpollLink.containsKey(Guild))
							sendMessage(Channel, "\"" + StrawpollTitle.get(Guild) + "\" -> http://www.strawpoll.me/" + StrawpollLink.get(Guild));
						else
							sendMessage(Channel, User + ":  There is currently no poll active.  Use \"!poll new\" to start a new poll.");
						return;
					}
					if (Args[0].equalsIgnoreCase("new"))
					{
						if (StrawpollLink.containsKey(Guild))
							sendMessage(Channel, "There is already a poll running at http://www.strawpoll/me/" + StrawpollLink.get(Guild) + ".  Use \"!poll stop\" to stop tracking this poll.");
						else
						{
							String Title = Message.substring(Message.indexOf("new")+4, Message.indexOf("|")-1);
							String[] ItemsTest = Message.split(Pattern.quote("|"));
							int Options = ItemsTest.length-1;
							if (Options < 2)
							{
								sendMessage(Channel, "Not enough options.");
								return;
							}
							String[] Items = new String[Options];
							for (int i = 1; i <= Options; i++)
								Items[i-1] = Message.split(Pattern.quote("|"))[i].trim();
							
							String Link = CreateStrawpoll(Title, Items);
							sendMessage(Channel, "New poll \"" + Title + "\" created!  Vote here -> http://www.strawpoll.me/" + Link);
							StrawpollLink.put(Guild, Link);
							StrawpollTitle.put(Guild, Title);
								
							try
							{
								Statement State = Conn.createStatement();
								State.execute("INSERT INTO `polls`(`Channel`, `Title`, `URL`) VALUES ('" + Guild + "','" + Title + "','" + Link + "')");
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
					}
					if (Args[0].equalsIgnoreCase("results"))
					{
						if (!StrawpollLink.containsKey(Guild))
							sendMessage(Channel, User + ":  There is currently no poll active.");
						else
							StrawpollResults(Channel, Guild);
					}
					if (Args[0].equalsIgnoreCase("stop"))
					{
						if (StrawpollLink.containsKey(Guild))
						{
							sendMessage(Channel, "Stopped tracking poll http://www.strawpoll.me/" + StrawpollLink.get(Guild) + ".");
							StrawpollLink.remove(Guild);
							StrawpollTitle.remove(Guild);
							
							try
							{
								Statement State = Conn.createStatement();
								State.execute("DELETE FROM `polls` WHERE `polls`.`Channel` = '" + Guild + "'");
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
						else
							sendMessage(Channel, "There is currently no poll active.");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
			
			if (Command.equalsIgnoreCase("!shoutout") && isMod(Mess))
			{
				if (Message.split(" ").length > 1)  
				{
					URL Strim;						

					// This chunk parses the JSON response of Twitch's API from the channel in question and outputs a shoutout.
					try 
					{
						Strim = new URL("https://api.twitch.tv/kraken/channels/" + Message.split(" ")[1]);
						URLConnection Connect = Strim.openConnection();
						Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
						InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
						Page Items = new Gson().fromJson(Reader,  Page.class);
						sendMessage(Channel, "Yoooooo!  Go check out " + Items.display_name + ", another awesome streamer!  They were last seen playing " + Items.game + "!  " + Items.url + "  p4ntzHype");
					}
					catch (FileNotFoundException e)
					{
						sendMessage(Channel, User + ":  User not found.  Perhaps try spelling it properly, scrub.  sbzyKappa");
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				return;
			}
			
			if (Message.equalsIgnoreCase("!chance") && Channel.getID().equals("243969142133096449"))
			{
				Random Rand = new Random();
				int Result = Rand.nextInt(999) + 1;
				if (Result == 1000)  // 0.1%
					sendMessage(Channel, User + ":  Congratulations!  You won! p4ntzB");
				else
					if (Result > 950)
						sendMessage(Channel, User + ":  So close!");
					else
						if (Result > 680)
							sendMessage(Channel, User + ":  Getting there...");
						else
							if (Result > 400)
								sendMessage(Channel, User + ":  Nice try.");
							else
									sendMessage(Channel, User + ":  Not even close. sbzyKappa");
				return;
			}
			
			// For the user to add user-defined commands
			if (Command.equals("!addcom") && isMod(Mess))
			{
				AddCom(Message, Channel, Guild, User, true);
				return;
			}
			
			// For the user to delete user-defined commands
			if (Command.equals("!delcom") && isMod(Mess))
			{
				DelCom(Message, Channel, Guild, User, true);
				return;
			}
			
			// For the user to edit user-defined commands
			if (Command.equals("!editcom") && isMod(Mess))
			{
				if (Message.length() <= 8 || (!Message.split(" ")[1].startsWith("!") && !Message.split(" ")[1].equals("-M")))
					sendMessage(Channel, User + ":  Usage:  !editcom (-M if Mod-Only) ![command name] [output]");
				else
				{
					int Num = 1;
					boolean Found = DelCom(Message, Channel, Guild, User, false);
					AddCom(Message, Channel, Guild, User, false);
					if (Message.split(" ")[1].equals("-M"))
						Num = 2;
					if (Found)
						sendMessage(Channel, User + ":  Command " + Message.split(" ")[Num] + " edited.");
					else
						sendMessage(Channel, User + ":  Command " + Message.split(" ")[Num] + " not found.");
				}
				return;
			}
			
			if (Command.equals("!stream") && (Mess.getGuild().getOwner().mention(false).equals(Mess.getAuthor().mention(false)) || Mess.getAuthor().mention(false).equals("<@113423696147808256>")))
			{
				
				if (Message.equals("!stream"))
				{
					sendMessage(Channel, User + ":  Usage:  !stream add @DiscordName ChannelName");
					return;
				}
				if (Args[0].equalsIgnoreCase("add"))
				{
					if (Args.length != 3)
					{
						sendMessage(Channel, User + ":  Usage:  !stream add @DiscordName ChannelName");
						return;
					}
					
					Statement State = Conn.createStatement();
					State.execute("SELECT * FROM `" + Guild + "streams` WHERE 1");
					ResultSet Result = State.getResultSet();
					
					while (Result.next())
					{
						try
						{
							if (Result.getString(1).equals(getUser(Mess.getGuild(), Args[1]).mention(false)))
							{
								sendMessage(Channel, "Stream is already added.");
								return;
							}
						}
						catch (Exception e)
						{
							sendMessage(Channel, e.getMessage());
						}
					}
					
					IUser NewUser = null;
					try
					{
						NewUser = getUser(Mess.getGuild(), Args[1]);
					}
					catch (Exception e)
					{
						if (e.getMessage().startsWith("Discord"))
						{
							sendMessage(Channel, e.getMessage());
							return;
						}
					}
					
					String URL = getStreamURL(Args[2]);
					if (URL.startsWith("User"))
					{
						sendMessage(Channel, URL);
						return;
					}
					
					try
					{
						State.execute("SELECT * FROM `" + Guild + "streams`");
					}
					catch (SQLSyntaxErrorException e)
					{
						State.execute("CREATE TABLE `discord`.`" + Guild + "streams` ( `UserID` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `URL` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , PRIMARY KEY (`UserID`)) ENGINE = InnoDB;");
					}
					State.execute("INSERT INTO `" + Guild + "streams` (`UserID`,`URL`) VALUES ('" + NewUser.mention(false) + "','" + URL + "')");
					sendMessage(Channel, "Stream successfully added.");
					return;
				}
				if (Args[0].equalsIgnoreCase("remove"))
				{
					if (Args.length != 2)
					{
						sendMessage(Channel, User + ":  Usage:  !stream remove @DiscordName");
						return;
					}
					Statement State = Conn.createStatement();
					try
					{
						State.execute("SELECT * FROM `" + Guild + "streams`");
					}
					catch (SQLSyntaxErrorException e)
					{
						State.execute("CREATE TABLE `discord`.`" + Guild + "streams` ( `UserID` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `URL` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , PRIMARY KEY (`UserID`)) ENGINE = InnoDB;");
					}
					try
					{
						State.execute("DELETE FROM `" + Guild + "streams` WHERE `" + Guild + "streams`.`UserID` = '" + Args[1] + "';");
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
					
					
					return;
				}
				if (Args[0].equalsIgnoreCase("channel"))
				{
					if (Args.length != 2 || !Args[1].startsWith("<#"))
					{
						sendMessage(Channel, User + ":  Usage:  !stream channel #ChannelName");
						return;
					}
					try
					{
						IChannel Chan = getChannel(Mess.getGuild(), Args[1]);
						Statement State = Conn.createStatement();
						try
						{
							State.execute("SELECT * FROM `channels` WHERE `Guild` = '" + Guild + "'");
							State.execute("UPDATE `channels` SET `OtherChannel`='" + Chan.mention() + "' WHERE `Guild` = '" + Guild + "'");
						}
						catch (SQLException e)
						{
							State.execute("INSERT INTO `channels`(`Guild`, `Owner`, `OwnerChannel`, `OtherChannel`) VALUES ('" + Guild + "', NULL, NULL,'" + Chan.mention() + "'");
						}
						sendMessage(Channel, "Channel successfully added.");
						sendMessage(Chan, "I will post non-owner stream announcements here.");
					}
					catch (Exception e)
					{
						if (e.getMessage().startsWith("Channel"))
						{
							sendMessage(Channel, e.getMessage());
							return;
						}
						sendMessage(Channel, "An unknown error occurred.");
						return;
					}
				}
				
				if (Args[0].equalsIgnoreCase("owner"))
				{
					if (Args.length != 3 || !Args[1].startsWith("<@") || !Args[2].startsWith("<#"))
					{
						sendMessage(Channel, User + ":  Usage:  !stream owner @DiscordName #AnnouncementChannel");
						return;
					}
					IUser Owner = null;
					IChannel Chan = null;
					try
					{
						Owner = getUser(Mess.getGuild(), Args[1]);
						Chan = getChannel(Mess.getGuild(), Args[2]);
					}
					catch (Exception e)
					{
						if (e.getMessage().startsWith("Discord"))
						{
							sendMessage(Channel, e.getMessage());
							return;
						}
						if (e.getMessage().startsWith("Channel"))
						{
							sendMessage(Channel, e.getMessage());
							return;
						}
					}
					
					
					/*PrintWriter Writer = new PrintWriter(new FileOutputStream("Discord\\Streams\\YateGuildOwners.txt", true));
					Writer.println(Owner.mention(false) + "~" + Mess.getGuild().getName());
					Writer.flush();
					Writer.close();
					
					Writer = new PrintWriter(new FileOutputStream("Discord\\Streams\\YateOwnerChannels.txt", true));
					Writer.println(Mess.getGuild().getName() + "~" + Args[2]);
					Writer.flush();
					Writer.close();*/
					
					Statement State = Conn.createStatement();
					try
					{
						State.execute("SELECT * FROM `channels` WHERE `Guild` = '" + Guild + "'");
						State.execute("UPDATE `channels` SET `Owner`='" + Owner.mention(false) + "',`OwnerChannel` = '" + Args[2] + "' WHERE `Guild` = '" + Guild + "'");
					}
					catch (SQLException e)
					{
						State.execute("INSERT INTO `channels` (`Guild`, `Owner`, `OwnerChannel`, `OtherChannel`) VALUES ('" + Guild + "', '" + Owner.mention(false) + "','" + Args[2] + "',NULL)");
					}
					
					sendMessage(Channel, "Owner successfully added.");
					sendMessage(Chan, "I will post stream announcements related to " + Owner.mention(false) + " here.");
					
					return;
				}
				return;
			}
			
			if (Command.equals("!commands"))
			{
				sendMessage(Channel, "Find my documentation here  ->  http://discord.yatebot.yatekko.com");
				return;
			}
			
			if (Command.equalsIgnoreCase("!YateBot"))
				sendMessage(Channel, "Click here to bring YateBot into your Discord server!  ->  http://discord.yatekko.com");
			
			if (Command.equals("!nickname") && isOwner(Mess))
			{
				if (Args == null || Args.length != 1)
					return;
				Statement State = Conn.createStatement();
				try
				{
					State.execute("SELECT * FROM `botnickname` WHERE `Guild` = '" + Mess.getGuild().getID() + "'");
					ResultSet Result = State.getResultSet();
					if (!Result.next())
						throw new SQLException("");
					State.execute("UPDATE `botnickname` SET `Nickname`='" + Args[0] + "' WHERE `Guild` = '" + Mess.getGuild().getID() + "'");
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					State.execute("INSERT INTO `botnickname`(`Guild`, `Nickname`) VALUES ('" + Mess.getGuild().getID() + "', '" + Args[0] + "')");
				}
				try
				{
					Mess.getGuild().setUserNickname(Bot.getOurUser(), Args[0]);
				}
				catch (RateLimitException | DiscordException e)
				{
					e.printStackTrace();
					sendMessage(Channel, "My name is being changed too often.  Try again later.");
					return;
				}
				sendMessage(Channel, "My name has been updated.");
				return;
			}
			
			if (Command.equals("!clear") && isMod(Mess))
			{
				if (Message.equals("!clear"))
				{
					sendMessage(Channel, User + ":  Usage:  !clear [Amount] [@User]  (@User is optional)");
					return;
				}
				if (Args.length == 1)
				{
					if (!isInteger(Args[0]))
					{
						sendMessage(Channel, User + ":  You must provide the amount to clear as a number.");
						return;
					}
					List<IMessage> Messages = getChannelMessages(Mess.getChannel());
					for (int i = 0; i <= Integer.parseInt(Args[0]); i++)
					{
						Messages.get(i).delete();
					}
				}
				else
				{
					if (!isInteger(Args[0]))
					{
						sendMessage(Channel, User + ":  You must provide the amount to clear as a number.");
						return;
					}
					
					IUser MessageUser;
					try
					{
						MessageUser = getUser(Mess.getGuild(), Args[1]);
					}
					catch (Exception e)
					{
						if (e.getMessage().startsWith("Discord"))
						{
							sendMessage(Channel, User + ":  User not found.");
							return;
						}
						sendMessage(Channel, "An unknown error occurred.");
						e.printStackTrace();
						return;
					}
					List<IMessage> Messages = getUserMessages(MessageUser, Mess.getChannel());
					for (int i = 0; i < Integer.parseInt(Args[0]); i++)
					{
						Messages.get(i).delete();
					}
					Mess.delete();
				}
				sendMessage(Channel, "Deleted " + Args[0] + " message(s).");
				
				int Delay = 3000;
				final Timer Time = new Timer(Delay, null);
				Time.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent Ev)
					{
						try
						{
							IMessage Alert = getUserMessages(Bot.getOurUser(), Mess.getChannel()).get(0);
							Alert.delete();
						}
						catch (MissingPermissionsException | RateLimitException | DiscordException e)
						{
							e.printStackTrace();
						}
					}
				});
				Time.setRepeats(false);
				Time.start();
				
				return;
			}
			
			if (Command.equals("!announce") && Mess.getAuthor().getID().equals("113423696147808256"))
			{
				if (Message.equals("!announce"))
					return;
				for (int i = 0; i < Bot.getGuilds().size(); i++)
				{
					sendMessage(Bot.getGuilds().get(i).getChannelByID(Bot.getGuilds().get(i).getID()), "```" + Message.substring(10) + "\n\nIf you have a question you would like to ask me, DM YateBot with it and he will forward it to me.  I apologize for any inconveniences.\n\n- Yatekko```");
				}
				return;
			}
			
			///////////////////////////////////////////////////////////////////////
			//////////////////////////////  Music Bot  ////////////////////////////
			///////////////////////////////////////////////////////////////////////
			
			if (Command.equals("!music"))
			{
				if (Message.equals("!music"))
					return;
				
				if (Args[0].equals("help"))
				{
					sendMessage(Channel, "Command:  !music <function> [args]\nDescription:  Play your favorite songs straight from YouTube.\nParameters:\n  **play** [URL] - Start playing a song from YouTube\n  ***songlist*** - Lists the songs currently in the queue\n  **pause** - Pause the current song *DJ / Mod Only*\n  **unpause** - Unpauses the current song *DJ / Mod Only*\n  **volume** [0 - 100] - Changes the volume of the music *DJ / Mod Only*\n  **skip** - Skips the current song *DJ / Mod Only*\n  Example:\n!music play <https://www.youtube.com/watch?v=IzhMzY5avLI>");
					return;
				}
				
				if (Args[0].equals("songlist"))
				{
					StringBuilder Queue = new StringBuilder();
					for (int i = 0; i < SongQueue.size(); i++)
						Queue = Queue.append("`" + SongQueue.get(i) + "`, ");
					try
					{
						Queue.delete(Queue.length()-2, Queue.length());
					}
					catch (StringIndexOutOfBoundsException e)
					{
						sendMessage(Channel, "There is no music in the queue.");
						return;
					}
					sendMessage(Channel, Queue.toString());
					return;
				}
				
				IVoiceChannel Chan;
				if (Guild.equals("Giggle Assassins") && StreamsLive.contains("<@196066340564369418>"))
				{
					IUser Steph = getUser(Mess.getGuild(), "<@196066340564369418>");
					Chan = Steph.getConnectedVoiceChannels().get(0);
				}
				else
				{
					List<IVoiceChannel> VoiceChans = Event.getMessage().getAuthor().getConnectedVoiceChannels();
					if (VoiceChans.isEmpty())
					{
						sendMessage(Channel, "[**Music**]  You are not in a voice channel.");
						return;
					}
					Chan = VoiceChans.get(0);
				}
				
				if (Args[0].equals("volume") && (isMod(Mess) || isDJ(Mess)))
				{
					if (Args.length != 2)
					{
						sendMessage(Channel, User + ":  Usage:  !music volume 1-100");
						return;
					}
					try
					{
						if (Math.signum(Double.parseDouble(Args[1])) != 1 || Integer.parseInt(Args[1]) < 1 || Integer.parseInt(Args[1]) > 100)
						{
							sendMessage(Channel, User + ":  Number must be between 1 and 100.");
							return;
						}
						AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Mess.getGuild());
						Player.setVolume(Float.parseFloat(Args[1])/100);
					}
					catch (NumberFormatException e)
					{
						sendMessage(Channel, User + ":  You can only enter a number.");
						return;
					}
				}
				
				if (Args[0].equals("play"))
				{
					try
					{
						if (Args.length < 2)
						{
							sendMessage(Channel, User + ":  Usage:  `!music play [YoutubeURL]` or `!music play [Search Term]`");
							return;
						}
						if (MusicDownloading)
						{
							sendMessage(Channel, User + ":  Please wait until the previous song finishes adding to the playlist.");
							return;
						}
						MusicDownloading = true;
						boolean URLFound;
						Channel.setTypingStatus(true);
						String ID = null;
						String Pat = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|‌​%2Fvideos%2F|embed%2‌​F|youtu.be%2F|%2Fv%2‌​F)[^#\\&\\?\\n]*";
						Pattern CompiledPattern = Pattern.compile(Pat);
						Matcher Match = CompiledPattern.matcher(Args[1]);
						if (Match.find())
						{
							URLFound = true;
							ID = Match.group();
						}
						else
						{
							URLFound = false;
							/*ID = null;
							Process Proc = Runtime.getRuntime().exec("youtube-dl \"ytsearch:" + Message.substring(6) + "\" --get-id");
							Proc.waitFor();
							BufferedReader Reader = new BufferedReader(new InputStreamReader(Proc.getInputStream()));
							String CurrentLine = null;
							while ((CurrentLine = Reader.readLine()) != null)
								ID = CurrentLine;*/
						}
						
						AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Mess.getGuild());
						Process Proc;
						if (URLFound)
						{
							Proc = Runtime.getRuntime().exec("youtube-dl https://www.youtube.com/watch?v=" + ID + " --print-json --extract-audio --audio-format wav --prefer-ffmpeg --ffmpeg-location bin/ffmpeg.exe --id --call-home");
						}
						else
						{
							Proc = Runtime.getRuntime().exec("youtube-dl \"ytsearch:" + Message.substring(12) + "\" --print-json --extract-audio --audio-format wav --prefer-ffmpeg --ffmpeg-location bin/ffmpeg.exe --id --call-home");
						}
						
						//  Delete calling command and output confirmation
						Mess.delete();
						//Proc.waitFor();
						
						/*URL Strim = new URL("https://noembed.com/embed?url=https://www.youtube.com/watch?v=" + ID);
						URLConnection Connect = Strim.openConnection();
						InputStreamReader IRReader = new InputStreamReader(Connect.getInputStream());*/
						
						
						JsonParser Parser = new JsonParser();
						InputStreamReader IRReader = new InputStreamReader(Proc.getInputStream());
						JsonObject Obj = Parser.parse(IRReader).getAsJsonObject();
						
						String Length = Obj.get("duration").toString();
						Length = String.format("%02d:%02d", TimeUnit.SECONDS.toMinutes(Integer.parseInt(Length)), Integer.parseInt(Length) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(Integer.parseInt(Length))));
						
						sendMessage(Channel, "[**Music**]" + "  " + Mess.getAuthor().mention(false) + " has added `" + Obj.get("uploader").toString().substring(1, Obj.get("uploader").toString().length()-1) + " - " + Obj.get("fulltitle").toString().substring(1, Obj.get("fulltitle").toString().length()-1) + "` to the queue.  (`" + Length + "`)");
						Channel.setTypingStatus(false);
						AudioInputStream AudioStream = AudioSystem.getAudioInputStream(new File(Obj.get("id").toString().substring(1, Obj.get("id").toString().length()-1) + ".wav"));
						Player.queue(AudioStream);
						SongQueue.add(Obj.get("fulltitle").toString().substring(1, Obj.get("fulltitle").toString().length()-1));
						
						if (Player.getPlaylistSize() == 1)
						{
							Chan.join();
							Player.setVolume((float) 0.1);
							MusicCheck(Mess.getGuild());
						}
						MusicDownloading = false;
						return;
					}
					catch (Exception e)
					{
						e.printStackTrace();
						sendMessage(Channel, "An error occurred.");
						Channel.setTypingStatus(false);
						MusicDownloading = false;
						return;
					}
				}
				
				if (Args[0].equals("skip") && (isMod(Mess) || isDJ(Mess)))
				{
					AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Mess.getGuild());
					Player.skip();
					SongQueue.remove(0);
					return;
				}
				
				if (Args[0].equals("pause") && (isMod(Mess) || isDJ(Mess)))
				{
					AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Mess.getGuild());
					Player.setPaused(true);
					return;
				}
				
				if (Args[0].equals("unpause") && (isMod(Mess) || isDJ(Mess)))
				{
					AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Mess.getGuild());
					Player.setPaused(false);
				}
			}
			
			if (Command.equals("!name") && isOwner(Event.getMessage()))
			{
				if (Args == null)
				{
					sendMessage(Channel, "You must give me a name!");
					return;
				}
				Event.getMessage().getGuild().setUserNickname(Bot.getOurUser(), Message.substring(6));
				sendMessage(Channel, "My name has been changed!");
				try
				{
					Statement State = Conn.createStatement();
					State.execute("SELECT * FROM `botnickname` WHERE `Guild` = '" + Mess.getGuild().getID() + "'");
					ResultSet Result = State.getResultSet();
					if (Result.next())
						State.execute("UPDATE `botnickname` SET `Nickname` = '" + Message.substring(6) + "' WHERE `Guild` = '" + Mess.getGuild().getID() + "'");
					else
						State.execute("INSERT INTO `botnickname` (`Guild`, `Nickname`) VALUES ('" + Mess.getGuild().getID() + "', '" + Message.substring(6) + "')");
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			
			if (Command.equals("!roll") || Command.equals("!r"))
			{
				if (Args == null)
				{
					sendMessage(Channel, User + ":  Usage:  `!roll [# dice]d[# sides] + [modifiers]`  Shortcut is `!r`");
					return;
				}
				String Content;
				if (Command.equals("!roll"))
					Content = Message.substring(6).replaceAll("\\s+", "");
				else
					Content = Message.substring(3).replaceAll("\\s+", "");
				boolean HasModifier = false;
				StringBuilder Out = new StringBuilder();
				String Pat = "(\\d{1,2}[dD]\\d{1,3}\\+\\d{1,10})|(\\d{1,2}[dD]\\d{1,3})";
				Pattern CompiledPattern = Pattern.compile(Pat);
				Matcher Match = CompiledPattern.matcher(Content);
				
				if (Match.find())
				{
					int Dice = Integer.parseInt(Content.split("\\D")[0]);  // ( 9 )  ( d20+90 )
					int Sides = Integer.parseInt(Content.split("\\D")[1]);  //  ( 20 ) ( +90)
					int Modifier = 0;
					int Result = 0;
					if (Content.split("\\D").length > 2)
					{
						HasModifier = true;
						Modifier = Integer.parseInt(Content.split("\\D")[2]);
					}
					
					Out.append(Mess.getAuthor().getName() + " rolled ");
					Random Rand = new Random();
					for (int i = 0; i < Dice; i++)
						RolledDice.add(Rand.nextInt(Sides)+1);
					
					Out.append("( ");
					for (int i = 0; i < RolledDice.size(); i++)
					{
						Out.append("`" + RolledDice.get(i) + "`" + " + ");
						Result += RolledDice.get(i);
					}
					Out.delete(Out.length()-3, Out.length());
					Out.append(" )");
					if (HasModifier)
					{
						Out.append(" + `" + Modifier + "`");
						Result += Modifier;
					}
					Out.append(" = `" + Result + "`");
					sendMessage(Channel, Out.toString());
					RolledDice.clear();
					return;
				}
				else
				{
					sendMessage(Channel, User + ":  Usage:  `!roll [# dice]d[# sides] + [modifiers]`  Shortcut is `!r`");
					return;
				}
			}
			
			if (Command.equals("!percent"))
			{
				Random Rand = new Random();
				sendMessage(Channel, Mess.getAuthor().getName() + " rolled `" + (Rand.nextInt(100)+1) + "`");
				return;
			}
			
			if (Command.equals("!8ball"))
			{
				if (Args == null)
					return;
				Random Rand = new Random();
				sendMessage(Channel, EightBall[Rand.nextInt(19)]);
				return;
			}
			
			//////////////////////////////////////////////////////////////
			////////////////////   Custom Commands   /////////////////////
			//////////////////////////////////////////////////////////////
			
			if (Command.equals("!rule34") && Mess.getGuild().getID().equals("252283961013829636"))
			{
				if (Args == null)
					return;
				String[] MessArr = Message.substring(8).split(" ");
				StringBuilder StringMess = new StringBuilder();
				for (int i = 0; i < MessArr.length; i++)
				{
					StringMess.append(MessArr[i].concat("_"));
				}
				String Search = StringMess.toString().substring(0, StringMess.toString().length()-1);
				
				URL Strim = new URL("http://rule34.xxx/index.php?page=dapi&s=post&q=index&tags=" + Search);
				SAXReader Reader = new SAXReader();
				Document Doc = Reader.read(Strim);
				String CurrentLine;
				for (int i = 0; i < Doc.getRootElement().nodeCount(); i++)
				{
					CurrentLine = Doc.getRootElement().node(i).asXML();
					if (!CurrentLine.contains("file_url"))
						continue;
					CurrentLine = CurrentLine.substring(CurrentLine.indexOf("//")+2, CurrentLine.indexOf("parent_id")-2);
					Rule34.add(CurrentLine);
				}
				Random Rand = new Random();
				int Result = 0;
				try
				{
					Result = Rand.nextInt(Rule34.size());
				}
				catch (IllegalArgumentException e)
				{
					sendMessage(Channel, "No results found.");
					Rule34.clear();
					return;
				}
				sendMessage(Channel, "http://" + Rule34.get(Result));
				Rule34.clear();
				return;
			}
			
			/////////////////////////////////
			// Default     /////////////////
			////////////////////////////////
			if (Message.startsWith("!"))
			{
				String Table = Guild;
				if (Command.equalsIgnoreCase("!prizes") || Command.equalsIgnoreCase("!fame"))
					Table = "rim99";
				Statement State = Conn.createStatement();
				State.execute("SELECT * FROM `" + Table + "`");
				ResultSet Result = State.getResultSet();
				while (Result.next())
				{
					if (Result.getInt(3) == 1)
					{
						if (Result.getString(1).equals(Command) && isMod(Mess))
							sendMessage(Channel, Result.getString(2));
					}
					else
					{
						if (Result.getString(1).equals(Command))
							sendMessage(Channel, Result.getString(2));
					}
				}
			}
			
		} // End Commands
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	////////////////////////////////////////////////////////
	////////////////// Helper Functions ////////////////////
	////////////////////////////////////////////////////////
	
	public String CreateStrawpoll(String Title, String[] Items) throws Exception
	{
		Strawpoll Poll = new Strawpoll(Title, Items);
		Gson GSON = new Gson();
		HttpClient Client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost Post = new HttpPost("https://strawpoll.me/api/v2/polls");
		StringEntity PostingString = new StringEntity(GSON.toJson(Poll));
		Post.setEntity(PostingString);
		Post.setHeader("Content-type", "application/json");
		HttpResponse Response = Client.execute(Post);
		HttpEntity Entity = Response.getEntity();
		InputStream Input = Entity.getContent();
		InputStreamReader Reader = new InputStreamReader(Input);
		StrawID StrawpollID = new Gson().fromJson(Reader, StrawID.class);

		return StrawpollID.id;
	}
	
	public void StrawpollResults(IChannel Channel, String Guild) throws Exception
	{
		HttpClient Client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost Post = new HttpPost("https://strawpoll.me/api/v2/polls/" + StrawpollLink.get(Guild));
		StringEntity PostingString = new StringEntity(
				"https://strawpoll.me/api/v2/polls/" + StrawpollLink.get(Guild));
		Post.setEntity(PostingString);
		Post.setHeader("Content-type", "application/json");
		HttpResponse Response = Client.execute(Post);
		HttpEntity Entity = Response.getEntity();
		InputStream Input = Entity.getContent();
		InputStreamReader Reader = new InputStreamReader(Input);
		StrawResults Results = new Gson().fromJson(Reader, StrawResults.class);

		String Winner = "";
		int WinnerVotes = 0;
		boolean[] Ties = new boolean[Results.votes.length];
		int TieNum = 0;
		for (int i = 0; i < Results.votes.length; i++) {
			if (Results.votes[i] > WinnerVotes) {
				WinnerVotes = Results.votes[i];
				Winner = Results.options[i];
			}
		}
		for (int i = 0; i < Results.votes.length; i++) {
			if (WinnerVotes == Results.votes[i]) {
				Ties[i] = true;
				TieNum++;
			}
		}

		if (WinnerVotes == 0)
			sendMessage(Channel, "There have been no votes yet.");
		else if (TieNum > 1) {
			String Message = "There is a tie between ";
			for (int i = 0; i < Results.votes.length; i++) {
				if (Ties[i])
					Message = Message.concat("\"" + Results.options[i] + "\"" + ", ");
			}
			Message = Message.substring(0, Message.length() - 2);
			Message = Message.concat("!");
			sendMessage(Channel, Message);
		} else {
			String Plural = "";
			if (WinnerVotes > 1)
				Plural = "s";
			sendMessage(Channel,
					"\"" + Winner + "\"" + " is currently winning with " + WinnerVotes + " vote" + Plural + "!");
		}
	}
	
	@SuppressWarnings("resource")
	public void AddCom(String Message, IChannel Channel, String Guild, String DisplayName, boolean UserCalled) throws SQLException
	{
		String Table = Guild;
		if (Message.split(" ")[0].equalsIgnoreCase("!prizes") || Message.split(" ")[0].equalsIgnoreCase("!fame"))
			Table = "rim99";
		if (Message.length() <= 7 || (!Message.split(" ")[1].startsWith("!") && !Message.split(" ")[1].equals("-M")))
			sendMessage(Channel, DisplayName + ":  Usage:  !addcom (-M if Mod-Only) ![command name] [output]");
		else {
			Statement State = null;
			try 
			{
				State = Conn.createStatement();
				State.execute("SELECT * FROM `" + Table + "` WHERE `Name` = '" + Message.split(" ")[1] + "'");
				ResultSet Result = State.getResultSet();
				if (Result.next())
				{
					sendMessage(Channel, DisplayName + ":  Command already exists.");
					return;
				}
				else
				{
					String CommandName = Message.substring(Message.indexOf(" !")+1);
					String Command = CommandName.substring(CommandName.indexOf(" ")+1);
					try
					{
						if (Table.equals("rim99"))
						{
							Connection TwitchConn = DriverManager.getConnection(TwitchDB);
							State = TwitchConn.createStatement();
						}
						else
							State = Conn.createStatement();
						if (Message.split(" ")[1].equalsIgnoreCase("-M"))
						{
							State.execute("INSERT INTO `" + Table + "`(`Name`, `Command`, `Mod`) VALUES ('" + Message.split(" ")[2] + "','" + Command + "','1')");
							if (UserCalled)
								sendMessage(Channel, DisplayName + ":  Command " + Message.split(" ")[2] + " added.");
							State.close();
							return;
						}
						else
						{
							State.execute("INSERT INTO `" + Table + "`(`Name`, `Command`, `Mod`) VALUES ('" + Message.split(" ")[1] + "','" + Command + "','0')");
							if (UserCalled)
								sendMessage(Channel, DisplayName + ":  Command " + Message.split(" ")[1] + " added.");
							State.close();
							return;
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
						State.close();
						return;
					}
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean DelCom(String Message, IChannel Channel, String Guild, String DisplayName, boolean UserCalled)
	{
		if (Message.length() <= 7 || (!Message.split(" ")[1].startsWith("!") && !Message.split(" ")[1].equals("-M")))
		{
			sendMessage(Channel, DisplayName + ":  Usage:  !delcom ![command name]");
			return false;
		}
		else 
		{
			try
			{
				String Table = Guild;
				if (Message.split(" ")[0].equalsIgnoreCase("!prizes") || Message.split(" ")[0].equalsIgnoreCase("!fame"))
					Table = "rim99";
					Statement State;
				if (Table.equals("rim99"))
				{
					Connection TwitchConn = DriverManager.getConnection(TwitchDB);
					State = TwitchConn.createStatement();
				}
				else
					State = Conn.createStatement();
				try
				{
					State.execute("SELECT * FROM `" + Table + "` WHERE `Name` = '" + Message.split(" ")[1] + "'");
					ResultSet Result = State.getResultSet();
					if (Result.next())
					{
						State.execute("DELETE FROM `" + Table + "` WHERE `Name` = '" + Message.split(" ")[1] + "'");
						if (UserCalled)
							sendMessage(Channel, DisplayName + ":  Command " + Message.split(" ")[1] + " deleted.");
						return true;
					}
					else
					{
						if (UserCalled)
							sendMessage(Channel, DisplayName + ":  Command " + Message.split(" ")[1] + " not found.");
						return false;
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					return false;
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	
	public String getStreamURL(String Channel)
	{
		String URL = "";
		try 
		{
			URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + Channel);
			URLConnection Connect = Strim.openConnection();
			Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
			InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
			Page Items = new Gson().fromJson(Reader, Page.class);
			URL = Items.url;
		} 
		catch (FileNotFoundException e) 
		{
			//return "User not found. Perhaps try spelling it properly. p4ntzREKT";
			URL = "User not found. Perhaps try spelling it properly. p4ntzREKT";
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return URL;
	}
	
	public void sendMessage(IChannel Channel, String Message)
	{
		/*try
		{
			new MessageBuilder(Bot).withChannel(Channel).withContent(Message).build();
		}
		catch (RateLimitException | DiscordException | MissingPermissionsException e)
		{
			e.printStackTrace();
		}*/
		RequestBuffer.request(() ->  // This will send the message when possible if it would encounter a RateLimitException.
		{
			try
			{
				new MessageBuilder(Bot).withChannel(Channel).withContent(Message).build();
			}
			catch (DiscordException | MissingPermissionsException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public boolean isMod(IMessage Message)
	{
		boolean Mod = false;
		List<IRole> Roles = Message.getAuthor().getRolesForGuild(Message.getGuild());
		if (Message.getAuthor().mention(false).equals("<@113423696147808256>"))
		{
			Mod = true;
			return Mod;
		}
		for (int i = 0; i < Roles.size(); i++)
		{
			if (Roles.get(i).getPermissions().toString().contains("ADMINISTRATOR") || Roles.get(i).getPermissions().toString().contains("MANAGE_CHANNELS") || Roles.get(i).getPermissions().toString().contains("MANAGE_CHANNEL") || Roles.get(i).getPermissions().toString().contains("MANAGE_SERVER"))
				Mod = true;
		}
		return Mod;
	}
	
	public boolean isOwner(IMessage Message)
	{
		boolean Owner = false;
		if (Message.getAuthor().mention(false).equals("<@113423696147808256>"))
			Owner = true;
		if (Message.getAuthor().equals(Message.getGuild().getOwner()))
			Owner = true;
		return Owner;
	}
	
	public boolean isDJ(IMessage Message)
	{
		boolean DJ = false;
		
		if (Message.getAuthor().mention(false).equals("<@113423696147808256>"))
		{
			DJ = true;
			return DJ;
		}
		for (int i = 0; i < Message.getAuthor().getRolesForGuild(Message.getGuild()).size(); i++)
		{
			if (Message.getAuthor().getRolesForGuild(Message.getGuild()).get(i).getName().equalsIgnoreCase("dj"))
			{
				DJ = true;
				return DJ;
			}
		}
		return DJ;
	}
	
	public IUser getUser(IGuild Guild, String UserString) throws Exception
	{	
		List<IUser> Users = Guild.getUsers();
		IUser User = null;
		boolean Found = false;
		
		for (int i = 0; i < Users.size(); i++)
		{
			if (UserString.equals(Users.get(i).mention(false)) || UserString.equals(Users.get(i).mention()))
			{
				User = Users.get(i);
				Found = true;
			}
		}
		if (!Found)
			throw new Exception("Discord user not found.");
		return User;
	}
	
	public IChannel getChannel(IGuild Guild, String ChannelString) throws Exception
	{
		List<IChannel> Channels = Guild.getChannels();
		IChannel Channel = null;
		boolean Found = false;
		
		for (int i = 0; i < Channels.size(); i++)
		{
			if (ChannelString.equals(Channels.get(i).mention()))
			{
				Channel = Channels.get(i);
				Found = true;
			}
		}
		
		if (!Found)
			throw new Exception("Channel not found.");
		return Channel;
	}
	
	public IVoiceChannel getVoiceChannel(IGuild Guild, IUser User) throws Exception
	{
		List<IVoiceChannel> Channels = Guild.getVoiceChannels();
		IVoiceChannel Channel = null;
		boolean Found = false;
		
		for (int i = 0; i < Channels.size(); i++)
		{
			if (Channels.get(i).getConnectedUsers().contains(User))
			{
				Channel = Channels.get(i);
				Found = true;
			}
		}
		
		if (!Found)
			throw new Exception("Channel not found.  Usage:  Use \"!music channel\" when inside the voice channel you wish to select.");
		return Channel;
	}
	
	public IVoiceChannel getVoiceChannelAuto(IGuild Guild, String ChannelString) throws Exception
	{
		List<IVoiceChannel> Channels = Guild.getVoiceChannels();
		IVoiceChannel Channel = null;
		boolean Found = false;
		
		for (int i = 0; i < Channels.size(); i++)
		{
			if (ChannelString.equalsIgnoreCase(Channels.get(i).getName()))
			{
				Channel = Channels.get(i);
				Found = true;
			}
		}
		
		if (!Found)
			throw new Exception("Channel not found.");
		return Channel;
	}
	
	public IGuild getGuild(String GuildString) throws Exception
	{
		List<IGuild> Guilds = Bot.getGuilds();
		IGuild Guild = null;
		boolean Found = false;
		
		for (int i = 0; i < Guilds.size(); i ++)
		{
			if (GuildString.startsWith("<@"))
			{
				if (GuildString.equals(Guilds.get(i).toString()))
				{
					Guild = Guilds.get(i);
					Found = true;
				}
			}
			else
			{
				if (GuildString.equals(Guilds.get(i).getName()))
				{
					Guild = Guilds.get(i);
					Found = true;
				}
			}
		}
		if (!Found)
			throw new Exception("Guild " + GuildString + " not found.");
		return Guild;
	}

	public void MusicCheck(IGuild Guild)
	{
		int Delay = 5000;
		final Timer Time = new Timer(Delay, null);
		Time.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Guild);
				if (Player.getPlaylistSize() == 0)
				{
					try
					{
						IVoiceChannel Chan = Bot.getConnectedVoiceChannels().get(0);
						Chan.leave();
						Time.stop();
						return;
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		Time.start();
	}
	
	public void MusicFileCleanup()
	{
		int Delay = 30000;
		//int Delay = 300000;
		final Timer Time = new Timer(Delay, null);
		Time.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				List<IGuild> Guilds = Bot.getGuilds();
				for (int i = 0; i < Guilds.size(); i++)
				{
					List<IVoiceChannel> Channels = Guilds.get(i).getVoiceChannels();
					for (int j = 0; j < Channels.size(); j++)
					{
						if (Channels.get(j).isConnected())
						{
							sendMessage(TestChannel, "Connected to voice - exiting loop.");
							return;
						}
					}
				}
				for (int i = 0; i < Guilds.size(); i++)
				{
					AudioPlayer Player = AudioPlayer.getAudioPlayerForGuild(Guilds.get(i));
					Player.clear();
					Player.clean();
				}
				File[] Files = FileFinder("C:\\Users\\darth\\workspace\\YateBot3\\YateBotTheThird\\Music");
				for (int i = 0; i < Files.length; i++)
				{
					boolean Complete = Files[i].delete();
					if (!Complete)
						System.out.println("Failed to delete " + Files[i].getName());
				}
			}
		});
		Time.start();
	}
	
	public File[] FileFinder( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".wav"); }
        } );

    }
	
	public static void ReverseArray(int[] b) 
	{
		   int left  = 0;          // index of leftmost element
		   int right = b.length-1; // index of rightmost element
		  
		   while (left < right) 
		   {
		      // exchange the left and right elements
		      int temp = b[left]; 
		      b[left]  = b[right]; 
		      b[right] = temp;
		     
		      // move the bounds toward the center
		      left++;
		      right--;
		   }
	}
	
	public static void ReverseArray(String[] b) 
	{
		   int left  = 0;          // index of leftmost element
		   int right = b.length-1; // index of rightmost element
		  
		   while (left < right) 
		   {
		      // exchange the left and right elements
		      String temp = b[left]; 
		      b[left]  = b[right]; 
		      b[right] = temp;
		     
		      // move the bounds toward the center
		      left++;
		      right--;
		   }
	}
	
	public List<IMessage> getUserMessages(IUser User, IChannel Channel)
	{
		List<IMessage> Messages = new ArrayList<IMessage>();
		for (int i = 0; i < Channel.getMessages().size(); i++)
		{
			if (Channel.getMessages().get(i).getAuthor().equals(User))
				Messages.add(Channel.getMessages().get(i));
		}	
		return Messages;
	}
	
	public List<IMessage> getChannelMessages(IChannel Channel)
	{
		List<IMessage> Messages = new ArrayList<IMessage>();
		for (int i = 0; i < Channel.getMessages().size(); i++)
		{
			Messages.add(Channel.getMessages().get(i));
		}
		return Messages;
	}
	
	public static boolean isInteger(String s)
	{
		return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix)
	{
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}
	
	////////////////////////////////////////////
	//////////////////  Classes  ///////////////
	////////////////////////////////////////////
	
	public class YouTubeProvider implements IAudioProvider 
	{

		private IAudioProvider provider;
		private boolean providerSet = false;

		public YouTubeProvider(String videoID) 
		{
			new Thread(() -> {
				Thread downloadThread = new Thread(() -> {
					try 
					{
						Process youtubeDl = new ProcessBuilder("youtube-dl", "-x", "--audio-format", "wav", "--id", "--", videoID).inheritIO().start();
						youtubeDl.waitFor();
					} 
					catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				});
				downloadThread.start();

				try 
				{
					downloadThread.join();
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}


				try 
				{
					provider = new FileProvider(new File(videoID + ".wav"));
					providerSet = true;
				} 
				catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}


				ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
				service.scheduleAtFixedRate(() -> {
					if (!this.isReady()) {
						new File(videoID + ".wav").delete();
					}
				}, 0, 1, TimeUnit.SECONDS);
			}).start();
		}

		@Override
		public boolean isReady() {
			return providerSet && provider.isReady();
		}

		@Override
		public byte[] provide() {
			return provider.provide();
		}
		
		public IAudioProvider GetProvider()
		{
			return provider;
		}
	}
	
	/*static class Youtube
	{
		String author_name;
		String title;
	}*/
	
	static class Youtube
	{
		String id;
		String uploader;
		String fulltitle;
		String duration;
	}
}
