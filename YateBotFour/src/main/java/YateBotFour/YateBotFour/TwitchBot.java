package YateBotFour.YateBotFour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;

/*import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;*/
import com.google.gson.Gson;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitchBot extends PircBotX
{
	
	User UserList[];
	String ChannelName;
	ArrayList<String> PointChannels = new ArrayList<String>();
	HashMap<String, Integer> Cooldown = new HashMap<String, Integer>();
	HashMap<String, Integer> KrystalGambleCooldown = new HashMap<String, Integer>();
	HashMap<String, String> Damageless = new HashMap<String, String>();
	HashMap<String, String> YTNames = new HashMap<String, String>();
	HashMap<String, String> Multi = new HashMap<String, String>();
	HashMap<String, Integer> Chambers = new HashMap<String, Integer>();
	HashMap<String, String> StrawpollLink = new HashMap<String, String>();
	HashMap<String, String> StrawpollTitle = new HashMap<String, String>();
	HashMap<String, String> TwitterToken = new HashMap<String, String>();
	HashMap<String, String> TwitterSecret = new HashMap<String, String>();
	HashMap<String, Long> TwitterID = new HashMap<String, Long>();
	HashMap<String, Twitter> Twitters = new HashMap<String, Twitter>();
	ArrayList<String> Waifus = new ArrayList<String>();
	ArrayList<String> Gamblers = new ArrayList<String>();
	ArrayList<String> Raiders = new ArrayList<String>();
	ArrayList<String> KrystalJoiners = new ArrayList<String>();
	//Twitter Twitter = TwitterFactory.getSingleton();
	//RequestToken RT;
	ActionListener LiveCheck;
	boolean Gambling = false;
	boolean Raiding = false;
	boolean KrystalJoined = false;
	boolean KrystalGambling = false;
	boolean Roulette = false;
	boolean ChanJoined = false;
	boolean StreamLive = false;
	String PointName;
	int GambleAmount;
	int RaidAmount;
	Connection Conn = null;
	private final String DiscordDB = "jdbc:mysql://localhost/discord?user=root&password=iMakeItRain590872&autoReconnect=true";
	private final String TwitchDB = "jdbc:mysql://localhost/twitch?user=root&password=iMakeItRain590872&autoReconnect=true";
	
	public TwitchBot(Configuration Config) throws SQLException
	{
		super(Config);
		Conn = DriverManager.getConnection(TwitchDB);
		PointChannels.add("#yatekko");
		PointChannels.add("#rim99");
		PointChannels.add("#tdlm1");
		PointChannels.add("#mangohberry");
		PointChannels.add("#girlamongguys");

		this.configuration.getListenerManager().addListener(new ListenerAdapter()
		{
			@Override
			public void onJoin(JoinEvent Event)
			{
				if (ChanJoined)
					return;
				ChanJoined = true;
				
				int Delay = 300000;
				LiveCheck = new ActionListener()
				{
					public void actionPerformed(ActionEvent Event)
					{ 
						if (GetChannel().getName().equalsIgnoreCase("#mangohberry"))
							return;
						try
						{
							StreamLive = isLive(GetChannel());
							if (StreamLive)
								PointUpdate(GetChannel());
						}
						catch (IOException | SQLException e)
						{
							e.printStackTrace();
						}
					}
				};
				new Timer(Delay, LiveCheck).start();
				
				try
				{
					Statement State = Conn.createStatement();
					State.execute("SELECT * FROM `PointNames` WHERE `Channel` = '" + GetChannel().getName().substring(1) + "'");
					ResultSet Result = State.getResultSet();
					if (Result.next())
						ChangeNick(Result.getString("Nickname"));
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				
				try 
				{
					Statement State = Conn.createStatement();
					State.execute("SELECT * FROM `PointNames` WHERE `Channel` = '" + GetChannel().getName() + "'");
					ResultSet Result = State.getResultSet();
					if (Result.next()) 
						PointName = Result.getString(2);
					else
						PointName = "points";
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				try
				{
					final String ChannelName = GetChannel().getName().substring(1);
					StreamLive = isLive(GetChannel());
					Statement StateCommands = Conn.createStatement();
					Statement StatePoints = Conn.createStatement();
					Statement StateQuotes = Conn.createStatement();
					Statement StateTargets = Conn.createStatement();
					try
					{
						StateCommands.execute("SELECT * FROM `" + ChannelName + "`");
					}
					catch (SQLSyntaxErrorException e)
					{
						StateCommands.execute("CREATE TABLE `twitch`.`" + ChannelName + "` ( `Name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Command` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Mod` BOOLEAN NOT NULL , PRIMARY KEY (`Name`)) ENGINE = InnoDB;");
					}
					try
					{
						StatePoints.execute("SELECT * FROM `" + ChannelName + "points`");
					}
					catch (SQLSyntaxErrorException e)
					{
						StatePoints.execute("CREATE TABLE `twitch`.`" + ChannelName + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
					}
					try
					{
						StateQuotes.execute("SELECT * FROM `" + ChannelName + "quotes`");
					}
					catch (SQLSyntaxErrorException e)
					{
						StateQuotes.execute("CREATE TABLE `twitch`.`" + ChannelName + "quotes` ( `QuoteNumber` INT(5) NOT NULL , `Quote` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , PRIMARY KEY (`QuoteNumber`)) ENGINE = InnoDB;");
					}
					try
					{
						StateTargets.execute("SELECT * FROM `" + ChannelName + "target`");
					}
					catch (SQLSyntaxErrorException e)
					{
						StateTargets.execute("CREATE TABLE `twitch`.`" + ChannelName + "target` ( `Target` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ) ENGINE = InnoDB;");
					}
				}
				catch(IOException | SQLException e)
				{
					e.printStackTrace();
				}
			}
			
			@Override
			public void onConnect(ConnectEvent Event) throws Exception
			{
				KrystalMoo();
				RimMoo();

				
			}
			
			@Override
			public void onMessage(MessageEvent Event)
			{
				String Command = Event.getMessage().toLowerCase();
				String DisplayName = Event.getTags().get("display-name");
				String User = Event.getUser().getLogin().toLowerCase();
				String[] Args = null;
				
				if (Event.getChannel().getName().equals("#rim99") && (Event.getMessage().contains(" rape ") || Event.getMessage().startsWith("rape")))
				{
					Event.getChannel().send().message("/timeout " + User + " 1");
					Event.getChannel().send().message(DisplayName + ":  We don't use that word here.");
					return;
				}
				if (!Event.getMessage().startsWith("!"))
					return;
				if (Event.getMessage().contains(" "))
				{
					Args = Event.getMessage().substring(Event.getMessage().indexOf(" ") + 1).split(" ");
					Command = Event.getMessage().split(" ")[0].toLowerCase();
				}
				
				if (Event.getMessage().contains("\u200B") && Event.getMessage().startsWith("!"))
				{
					Event.getChannel().send().message("Your command contains a zero-width space.  Zero-width spaces are evil.  Please do not use them.");
					return;
				}
				
				// For documentation
				if (Event.getMessage().equalsIgnoreCase("!commands") && OffCooldown("!commands"))
				{
					Event.getChannel().send().message("Find my documentation here  ->  http://yatebot.yatekko.com");
					Cooldown.put("!commands", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
				}
				
				// For the user to add user-defined commands
				if (Event.getMessage().startsWith("!addcom") && isMod(Event, User))
					AddCom(Event.getMessage(), Event.getChannel(), DisplayName, true);
				
				// For the user to delete user-defined commands
				if (Event.getMessage().startsWith("!delcom") && isMod(Event, User))
					DelCom(Event.getMessage(), Event.getChannel(), DisplayName, true);
				
				// For the user to edit user-defined commands
				if (Event.getMessage().startsWith("!editcom") && isMod(Event, User))
				{
					if (Event.getMessage().length() <= 8 || (!Event.getMessage().split(" ")[1].startsWith("!") && !Event.getMessage().split(" ")[1].equals("-M")))
						Event.getChannel().send().message(DisplayName + ":  Usage:  !editcom (-M if Mod-Only) ![command name] [output]");
					else
					{
						int Num = 1;
						boolean Found = DelCom(Event.getMessage(), Event.getChannel(), DisplayName, false);
						AddCom(Event.getMessage(), Event.getChannel(), DisplayName, false);
						if (Event.getMessage().split(" ")[1].equals("-M"))
							Num = 2;
						if (Found)
							Event.getChannel().send().message(DisplayName + ":  Command " + Event.getMessage().split(" ")[Num] + " edited.");
						else
							Event.getChannel().send().message(DisplayName + ":  Command " + Event.getMessage().split(" ")[Num] + " not found.");
					}
				}
				
				// To search through the text file of user-defined commands
				if (Event.getMessage().startsWith("!"))
				{
					try
					{
						String Table = Event.getChannel().getName().substring(1);
						if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!prizes") || Event.getMessage().split(" ")[0].equalsIgnoreCase("!fame"))
						{
							if (Event.getChannel().getName().equals("#rim99"))
								Table = "rim99";
						}
						Statement State = Conn.createStatement();
						State.execute("SELECT * FROM `" + Table + "`");
						ResultSet Result = State.getResultSet();
						while (Result.next())
						{
							if (Result.getInt(3) == 1)
							{
								if (Result.getString(1).equals(Event.getMessage().split(" ")[0].toLowerCase()) && isMod(Event, User) && OffCooldown(Command))
								{
									Event.getChannel().send().message(Result.getString(2));
									Cooldown.put(Command, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
								}
							}
							else
							{
								if (Result.getString(1).equals(Event.getMessage().split(" ")[0].toLowerCase()) && OffCooldown(Command))
								{
									Event.getChannel().send().message(Result.getString(2));
									Cooldown.put(Command, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
								}
							}
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				
				// To add a quote to the text file associated with that Event.getChannel().getName()
				if (Event.getMessage().startsWith("!addquote") && isMod(Event, User) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					if (Event.getMessage().length() <= 9)
						Event.getChannel().send().message(DisplayName + ":  Usage:  !addquote [quote]");
					else
						AddQuote(Event.getMessage().substring(10), Event.getChannel(), DisplayName, true);
				}
				
				// To delete a quote from the text file associated with that Event.getChannel().getName().
				if (Event.getMessage().startsWith("!delquote") && isMod(Event, User) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					if (Event.getMessage().length() > 10 && isInteger(Event.getMessage().split(" ")[1]))
						try {
							DelQuote(Event.getMessage(), Event.getChannel(), DisplayName, true, Event.getMessage());
						} catch (IOException e) {
							e.printStackTrace();
						}
					else
						Event.getChannel().send().message(DisplayName + ":  Usage:  !delquote [quote number]");
				}
				
				// To edit a quote from the text file associated with that Event.getChannel().getName().
				if (Event.getMessage().startsWith("!editquote") && isMod(Event, User) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					if (Event.getMessage().length() > 11 && isInteger(Event.getMessage().split(" ")[1]))
					{
						boolean Found = false;
						try {
							Found = DelQuote(Event.getMessage(), Event.getChannel(), DisplayName, false, Event.getMessage());
						} catch (IOException e) {
							e.printStackTrace();
						}
						AddQuote(Event.getMessage().substring(13), Event.getChannel(), DisplayName, false);
						if (Found)
							Event.getChannel().send().message(DisplayName + ":  Quote " + Event.getMessage().split(" ")[1] + " edited.");
						else
							Event.getChannel().send().message(DisplayName + ":  Quote " + Event.getMessage().split(" ")[1] + " not found.");
					}
					else
						Event.getChannel().send().message(DisplayName + ":  Usage:  !editquote [QuoteNum] [Quote]");
				}
				
				// Displays a random quote from that Event.getChannel().getName(), or a specific quote if given a number.
				if (Command.equals("!quote") && OffCooldown("!quote" + Event.getChannel().getName()) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					try
					{
						if (Args != null && isInteger(Args[0])) // User chooses a quote
						{
							Statement State = Conn.createStatement();
							State.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "quotes` ORDER BY `" + Event.getChannel().getName().substring(1) + "quotes`.`Index` ASC");
							ResultSet Result = State.getResultSet();
							Result.absolute(Integer.parseInt(Args[0]));
							if (Result.getRow() != Integer.parseInt(Args[0]))
							{
								Event.getChannel().send().message(DisplayName + ":  Quote #" + Args[0] + " not found.");
								return;
							}
							Event.getChannel().send().message(Result.getRow() + ":  " + Result.getString(2));
							return;
						}
						else // Else select a random quote
						{
							Statement State = Conn.createStatement();
							State.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "quotes` ORDER BY `" + Event.getChannel().getName().substring(1) + "quotes`.`Index` ASC");
							ResultSet Result = State.getResultSet();
							Result.last();
							Random Rand = new Random();
							int RandRow = Rand.nextInt(Result.getRow())+1;
							Result.absolute(RandRow);
							Event.getChannel().send().message(Result.getRow() + ":  " + Result.getString(2));
							return;
						}
					}
					catch (IllegalArgumentException e)
					{
						Event.getChannel().send().message("There are no quotes for this channel.");
						return;
					}
					catch (SQLIntegrityConstraintViolationException e)
					{
						Event.getChannel().send().message(DisplayName + ":  Quote already exists.");
						return;
					}
					catch (SQLException e)
					{
						if (e.getMessage().equals("Illegal operation on empty result set."))
						{
							Event.getChannel().send().message("There are no quotes for this channel.");
							return;
						}
						e.printStackTrace();
					}
				}
				
				//////////////////////////////////////////
				///////////  Global Commands  ////////////
				//////////////////////////////////////////
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!shoutout") && isMod(Event, User))
				{
					if (Event.getMessage().split(" ").length > 1)  
					{
						URL Strim;						

						  // This chunk parses the JSON response of Twitch's API from the Event.getChannel().getName() in question and outputs a shoutout.
							try 
							{
								Strim = new URL("https://api.twitch.tv/kraken/channels/" + Event.getMessage().split(" ")[1]);
								URLConnection Connect = Strim.openConnection();
								Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
								InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
								Page Items = new Gson().fromJson(Reader,  Page.class);
								Event.getChannel().send().message("Yoooooo!  Go check out " + Items.display_name + ", another awesome streamer!  They were last seen playing " + Items.game + "!  " + Items.url + "  p4ntzHype");
							}
							catch (FileNotFoundException e)
							{
								Event.getChannel().send().message(DisplayName + ":  User not found.  Perhaps try spelling it properly, scrub.  sbzyKappa");
							} 
							catch (IOException e)
							{
								e.printStackTrace();
							}
					}
				}
				
				if (Event.getMessage().equalsIgnoreCase("!YateLeave") && isBroadcaster(Event, User))
				{
					try
					{
						Statement State = Conn.createStatement();
						State.execute("UPDATE `channels` SET `LeaveEvent` = '1' WHERE `channels`.`Name` = '" + GetChannel().getName().substring(1) + "'");
						Event.getChannel().send().message("Leaving channel (may take up to a minute).");
						return;
					}
					catch (SQLException e)
					{
						e.printStackTrace();
						return;
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!YateJoin"))
				{
					try
					{
						if (Args != null)
						{
							URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + Event.getMessage().split(" ")[1]);
							URLConnection Connect = Strim.openConnection();
							Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
							InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
							
							Statement State = Conn.createStatement();
							State.execute("SELECT * FROM `channels` WHERE `Name` = '" + Event.getMessage().split(" ")[1] + "'");
							ResultSet Result = State.getResultSet();
							if (Result.next())
							{
								Event.getChannel().send().message("YateBot is already in channel " + Args[0] + "!");
								return;
							}
							Event.getChannel().send().message("Connecting to " + Args[0] + " (may take up to a minute)!");
							State.execute("INSERT INTO `channels`(`Name`, `LeaveEvent`) VALUES ('" + Event.getMessage().split(" ")[1] + "', 0)");
							Statement StateCommands = Conn.createStatement();
							Statement StatePoints = Conn.createStatement();
							Statement StateQuotes = Conn.createStatement();
							Statement StateTargets = Conn.createStatement();
							try
							{
								StateCommands.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "`");
							}
							catch (SQLSyntaxErrorException e)
							{
								StateCommands.execute("CREATE TABLE `twitch`.`" + Event.getChannel().getName().substring(1) + "` ( `Name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Command` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Mod` BOOLEAN NOT NULL , PRIMARY KEY (`Name`)) ENGINE = InnoDB;");
							}
							try
							{
								StatePoints.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "points`");
							}
							catch (SQLSyntaxErrorException e)
							{
								StatePoints.execute("CREATE TABLE `twitch`.`" + Event.getChannel().getName().substring(1) + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
							}
							try
							{
								StateQuotes.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "quotes`");
							}
							catch (SQLSyntaxErrorException e)
							{
								StateQuotes.execute("CREATE TABLE `twitch`.`" + Event.getChannel().getName().substring(1) + "` ( `Quotes` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Index` INT(4) NOT NULL , INDEX (`Index`)) ENGINE = InnoDB;");
							}
							try
							{
								StateTargets.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "target`");
							}
							catch (SQLSyntaxErrorException e)
							{
								StateTargets.execute("CREATE TABLE `twitch`.`" + Event.getChannel().getName().substring(1) + "target` ( `Target` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ) ENGINE = InnoDB;");
							}
							return;
						}
						else
						{
							Event.getChannel().send().message(DisplayName + ":  You must specify a channel to join.");
							return;
						}
					}
					catch (FileNotFoundException e)
					{
						Event.getChannel().send().message(DisplayName + ":  User not found.  Perhaps try spelling it properly, scrub.  sbzyKappa");
						return;
					}
					catch (SQLException | IOException e)
					{
						e.printStackTrace();
						return;
					}
				}
				
				if (Event.getMessage().equalsIgnoreCase("!uptime") && OffCooldown("!uptime"))
				{
					try {
						String Result = Uptime(Event.getChannel());
						Event.getChannel().send().message(Result);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Cooldown.put("!uptime", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					
				}
				
				if (Event.getMessage().equalsIgnoreCase("!game") && OffCooldown("!game"))
				{
					try {
						Game(Event.getMessage(), Event.getChannel(), DisplayName);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Cooldown.put("!game", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!title") && OffCooldown("!title"))
				{
					if (Event.getMessage().length() > 6 && isMod(Event, User))
					{
						/*try
						{
							String URL = "https://api.twitch.tv/kraken/channels/" + Event.getChannel().getName().toLowerCase().substring(1);// + "?oauth_token=oqxfkyp8nyr5qul88eak2m0za08jei";
							String Status = Event.getMessage().split(" ")[1];
							URL obj = new URL(URL);
							HttpURLConnection Connect = (HttpURLConnection) obj.openConnection();
							Connect.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
							Connect.setRequestProperty("Authorization", "OAuth <access_token>");
							Connect.setRequestMethod("PUT");
							Connect.setDoOutput(true);

							String Data = "Event.getChannel().getName()[status]=" + Status;
							
							OutputStreamWriter Writer = new OutputStreamWriter(Connect.getOutputStream());
							Writer.write(Data);
							Writer.flush();

							String Result = "";
							BufferedReader Reader = new BufferedReader(new InputStreamReader(Connect.getInputStream()));
							String Line = "";
							while((Line = Reader.readLine()) != null)
							{
								Result += Line;
							}
							Reader.close();
							Writer.close();
						} catch(IOException e)
						{
							System.out.println(e.getMessage());
						}*/
						
						/*HttpClient Client = HttpClients.createDefault();
						HttpPost Post = new HttpPost("https://api.twitch.tv/kraken/channels/" + Event.getChannel().getName().toLowerCase().substring(1));
						Post.addHeader("Accept", "application/vnd.twitchtv.v3+json");
						Post.addHeader("Authorization", "OAuth oqxfkyp8nyr5qul88eak2m0za08jei");
						Post.setEntity(new UrlEncodedFormEntity("Event.getChannel().getName()[status]=" + Event.getMessage().split(" ")[1]));*/
					}
					else
					{
						try 
						{
							Title(Event.getMessage(), Event.getChannel(), DisplayName);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
						Cooldown.put("!title", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!multi"))
				{
					if (!(Event.getMessage().length() > 6))
					{
						if (Multi.containsKey(Event.getChannel().getName()) && OffCooldown("!multi"))
						{
							Event.getChannel().send().message(Multi.get(Event.getChannel().getName()));
							Cooldown.put("!title", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
						}
						else
							Event.getChannel().send().message(DisplayName + ":  No multi currently set.  Mod Usage:  !multi [User1] [User2] ...");
					}
					else
					{
						if (isMod(Event, User))
						{
							if (Event.getMessage().split(" ")[1].equalsIgnoreCase("delete"))
							{
								Multi.remove(Event.getChannel().getName());
								Event.getChannel().send().message(DisplayName + "Multi deleted.");
							}
							else
							{
								StringBuilder URL = new StringBuilder("http://kadgar.net/live");
								String[] Names = Event.getMessage().substring(7).split(" ");
								for (int i = 0; i < Names.length; i++)
									URL.append("/" + Names[i]);
								Event.getChannel().send().message(URL.toString());
								Multi.put(Event.getChannel().getName(), URL.toString());
							}
						}
						else
							Event.getChannel().send().message(DisplayName + ":  Only mods may make new multi links.");
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!poll"))
				{
					try
					{
						if (Event.getMessage().length() < 9 && Event.getMessage().length() > 5)
							throw new UserException("");
						else
							if (Event.getMessage().equalsIgnoreCase("!poll"))
							{
								if (StrawpollLink.containsKey(Event.getChannel().getName()))
									Event.getChannel().send().message("\"" + StrawpollTitle.get(Event.getChannel().getName()) + "\" -> www.strawpoll.me/" + StrawpollLink.get(Event.getChannel().getName()));
								else
									Event.getChannel().send().message(DisplayName + ":  There is currently no poll active.  Use \"!poll new\" to start a new poll.");
							}
						if (Event.getMessage().length() > 5 && Event.getMessage().split(" ")[1].equalsIgnoreCase("new") && isBroadcaster(Event, User))
						{
							if (StrawpollLink.containsKey(Event.getChannel().getName()))
								Event.getChannel().send().message("There is already a poll running at www.strawpoll/me/" + StrawpollLink.get(Event.getChannel().getName()) + ".  Use \"!poll stop\" to stop tracking this poll.");
							else
							{
								String Title = Event.getMessage().substring(Event.getMessage().indexOf("new")+4, Event.getMessage().indexOf("|")-1);
								int Options = Event.getMessage().split(Pattern.quote("|")).length-1;
								if (Options < 2)
									throw new UserException("Not enough options.  ");
								String[] Items = new String[Options];
								for (int i = 1; i <= Options; i++)
									Items[i-1] = Event.getMessage().split(Pattern.quote("|"))[i].trim();
								
								String Link = CreateStrawpoll(Title, Items);
								Event.getChannel().send().message("New poll \"" + Title + "\" created!  Vote here -> www.strawpoll.me/" + Link);
								StrawpollLink.put(Event.getChannel().getName(), Link);
								StrawpollTitle.put(Event.getChannel().getName(), Title);
									
								/*try
								{
									File Output = new File("Polls.txt");
									PrintWriter Writer = new PrintWriter(new FileOutputStream(Output, true));
									Writer.println(Event.getChannel().getName() + "~" + StrawpollTitle.get(Event.getChannel().getName()) + "~" + StrawpollLink.get(Event.getChannel().getName()));
									Writer.flush();
									Writer.close();
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}*/
								try
								{
									Statement State = Conn.createStatement();
									State.execute("INSERT INTO `polls`(`Event.getChannel().getName()`, `Title`, `URL`) VALUES ('" + Event.getChannel().getName() + "','" + Title + "','" + Link + "')");
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
							}
						}
						if (Event.getMessage().length() > 5 && Event.getMessage().split(" ")[1].equalsIgnoreCase("results") && isMod(Event, User))
						{
							if (!StrawpollLink.containsKey(Event.getChannel().getName()))
								Event.getChannel().send().message(DisplayName + ":  There is currently no poll active.");
							else
								StrawpollResults(Event.getChannel());
						}
						if (Event.getMessage().length() > 5 && Event.getMessage().split(" ")[1].equalsIgnoreCase("stop") && isBroadcaster(Event, User))
						{
							if (StrawpollLink.containsKey(Event.getChannel().getName()))
							{
								Event.getChannel().send().message("Stopped tracking poll www.strawpoll.me/" + StrawpollLink.get(Event.getChannel().getName()) + ".");
								StrawpollLink.remove(Event.getChannel().getName());
								StrawpollTitle.remove(Event.getChannel().getName());
								
								/*try
								{
									File Input = new File("Polls.txt");
									File Temp = new File("PollsTemp.txt");
									BufferedReader Reader = new BufferedReader(new FileReader(Input));
									PrintWriter Writer = new PrintWriter(new FileOutputStream(Temp, true));
									String CurrentLine;
									
									while ((CurrentLine = Reader.readLine()) != null)
									{
										if (CurrentLine.split("~")[0].equals(Event.getChannel().getName()))
											continue;
										Writer.println(CurrentLine);
									}
									
									Reader.close();
									Writer.flush();
									Writer.close();
									Input.delete();
									Temp.renameTo(Input);
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}*/
								try
								{
									Statement State = Conn.createStatement();
									State.execute("DELETE FROM `polls` WHERE `polls`.`Event.getChannel().getName()` = '" + Event.getChannel().getName() + "'");
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
							}
							else
								Event.getChannel().send().message("There is currently no poll active.");
						}
					}
					
					catch (UserException e)
					{
						if (isBroadcaster(Event, User))
							Event.getChannel().send().message(DisplayName + ":  " + e.getLocalizedMessage() + "Usage:  !poll new [Question] | [Answer 1] | [Answer 2] | [Answer 3] ...");
						else
							Event.getChannel().send().message(DisplayName + ":  Only the broadcaster can start, stop, and get results of polls.");
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
				
				if (Command.equalsIgnoreCase("!test") && User.equals("yatekko"))
				{
					Event.getChannel().send().message(String.valueOf(isBroadcaster(Event, User)));
					return;
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!tweet"))
				{
					if (!TwitterToken.containsKey(User))
						Event.getChannel().send().message(DisplayName + ":  YateBot requires authentication.  Whisper YateBot \"!tweet\" for instructions.");
					else
					{
						if (Event.getMessage().equalsIgnoreCase("!tweet"))
								Event.getChannel().send().message(DisplayName + ":  Usage:  !tweet [Tweet to post]");
						else
						{
							Twitter Twitter = TwitterFactory.getSingleton();
							RequestToken RT;
							Twitter.setOAuthConsumer("JVDUcUNhuHrb0jDqD7G3GW0iW", "2WzpE7eO3QfWoatgTYSCxBwCSx6rmKKxLrdhjAva0ecSgT7taz");
							AccessToken AT = new AccessToken(TwitterToken.get(User), TwitterSecret.get(User), TwitterID.get(User));
							Twitter.setOAuthAccessToken(AT);
							try {
								Status Stat = Twitter.updateStatus(Event.getMessage().substring(7));
								Event.getChannel().send().message(DisplayName + ":  Successfully posted tweet.");
							} catch (TwitterException e) {
								e.printStackTrace();
							}
							
						}
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!add") && PointChannels.contains(Event.getChannel().getName()) && isBroadcaster(Event, User))  // Adding points - Syntax: ![pointname] [user] [amount]
				{
					if (Event.getMessage().split(" ").length != 3)
						Event.getChannel().send().message(DisplayName + ":  Usage:  !add [user] [amount]");
					else
					{
						try
						{
							if (Math.signum(Double.parseDouble(Event.getMessage().split(" ")[2])) != 1)
								Event.getChannel().send().message(DisplayName + ":  Number must be positive.");
							else
								Event.getChannel().send().message(AddPoints(Event.getMessage().split(" ")[1].toLowerCase(), Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[2])));
						} 
						catch (NumberFormatException | IOException e) 
						{
							Event.getChannel().send().message(DisplayName + ":  You can only add a number.");
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!remove") && PointChannels.contains(Event.getChannel().getName()) && isBroadcaster(Event, User))
				{
					if (Event.getMessage().split(" ").length != 3)
						Event.getChannel().send().message(DisplayName + ":  Usage:  !remove [user] [amount]");
					else
					{
						try
						{
							if (Math.signum(Double.parseDouble(Event.getMessage().split(" ")[2])) != 1)
								Event.getChannel().send().message(DisplayName + ":  Number must be positive.");
							else
								Event.getChannel().send().message(RemovePoints(Event.getMessage().split(" ")[1].toLowerCase(), Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[2])));
						} 
						catch (NumberFormatException | IOException | SQLException e) 
						{
							Event.getChannel().send().message(DisplayName + ":  You can only remove a number.");
						}
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!give") && PointChannels.contains(Event.getChannel().getName()))
				{
					if (Event.getMessage().split(" ").length != 3)
					{
						Event.getChannel().send().message(DisplayName + ":  Usage:  !give [user] [amount]");
						return;
					}
					if (Event.getMessage().split(" ")[2].startsWith("+") || Event.getMessage().split(" ")[2].startsWith("-"))
					{
						Event.getChannel().send().message(DisplayName + ":  You can only give a whole number.");
						return;
					}
					try
					{
						if (Math.signum(Double.parseDouble(Event.getMessage().split(" ")[2])) != 1)
						{
							Event.getChannel().send().message(DisplayName + ":  Number must be positive.");
							return;
						}
						if (Integer.parseInt(GetPoints(User, Event.getChannel()).split(" ")[0]) < Integer.parseInt(Event.getMessage().split(" ")[2]))
						{
							Event.getChannel().send().message(User + ":  You don't have enough " + PointName + ".");
							return;
						}
						
						URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + Event.getMessage().split(" ")[1]);
						URLConnection Connect = Strim.openConnection();
						Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
						InputStreamReader IRReader = new InputStreamReader(Connect.getInputStream());
						Page Items = new Gson().fromJson(IRReader, Page.class);
						
						RemovePoints(User.toLowerCase(), Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[2]));
						AddPoints(Event.getMessage().split(" ")[1].toLowerCase(), Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[2]));
						Event.getChannel().send().message(User + " gave " + Event.getMessage().split(" ")[2] + " " + PointName + " to " + Event.getMessage().split(" ")[1] + ".");
					}
					catch (NumberFormatException e)
					{
						Event.getChannel().send().message(DisplayName + ":  You can only give a whole number.");
						return;
					}
					catch (FileNotFoundException e)
					{
						Event.getChannel().send().message("User not found. Perhaps try spelling it properly. p4ntzREKT");
						return;
					}
					catch (IOException e)
					{ 
						e.printStackTrace();
						return;
					}
					catch (SQLException e)
					{
						e.printStackTrace();
						return;
					}
				}
				
				//if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!points") && (Event.getChannel().getName().equals("#rim99") || Event.getChannel().getName().equals("#yatekko") || Event.getChannel().getName().equals("#tdlm1")))
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!" + PointName) && PointChannels.contains(Event.getChannel().getName()))
				{
					if (Event.getMessage().equalsIgnoreCase("!" + PointName))
					{
						try 
						{
							String Points = GetPoints(User, Event.getChannel());
							Event.getChannel().send().message(DisplayName + ":  " + Points);
						} 
						catch (IOException | SQLException e) 
						{
							e.printStackTrace();
						}
					}
					else
					{
						try 
						{
							String Points = GetPoints(Event.getMessage().split(" ")[1].toLowerCase(), Event.getChannel());
							Event.getChannel().send().message(DisplayName + ":  " + Points);
						} 
						catch (IOException | SQLException e) 
						{
							e.printStackTrace();
						}
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!gamble") && PointChannels.contains(Event.getChannel().getName()) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					try
					{
						if (Gambling)
							Event.getChannel().send().message(DisplayName + ":  Gambling already started.  Type \"!join\" to join.");
						else
							if (Event.getMessage().equalsIgnoreCase("!gamble"))
								Event.getChannel().send().message(DisplayName + ":  You must specify how much you want to gamble.");
							else
							{
								if (Math.signum(Integer.parseInt(Event.getMessage().split(" ")[1])) == -1)
								{
									Event.getChannel().send().message(DisplayName + ":  Number must be positive.");
									return;
								}
								if (Math.signum(Integer.parseInt(Event.getMessage().split(" ")[1])) == 0)
								{
									Event.getChannel().send().message(DisplayName + ":  You cannot gamble nothing.");
									return;
								}
									int Points = Integer.parseInt(GetPoints(User, Event.getChannel()).split(" ")[0]);
									if (Points < Integer.parseInt(Event.getMessage().split(" ")[1]))
										Event.getChannel().send().message(DisplayName + ":  You don't have enough " + PointName);
									else
									{
										try
										{
											RemovePoints(User, Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[1]));
										}
										catch (IOException e)
										{
											e.printStackTrace();
										}
										Event.getChannel().send().message("Gambling has started!  Type \"!join\" to join!");
										Gamble(User, Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[1]));
									}
							}
					}
					catch (NumberFormatException e)
					{
						Event.getChannel().send().message(DisplayName + ":  You can only gamble a whole number.");
					} 
					catch (IOException e) 
					{
						if (e.getMessage().contains("response code: 422"))
						{
							Event.getChannel().send().message("User not found. Perhaps try spelling it properly. p4ntzREKT");
							return;
						}
						e.printStackTrace();
						return;
					}
					catch (SQLException e1)
					{
						
						e1.printStackTrace();
					}
				}
				
				if (Event.getMessage().equalsIgnoreCase("!join") && PointChannels.contains(Event.getChannel().getName()) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					try
					{
						if (!Gambling)
							Event.getChannel().send().message(DisplayName + ":  Gambling has not started.  Type \"!gamble [amount]\" to begin.");
						else
						{
							int Points = Integer.parseInt(GetPoints(User, Event.getChannel()).split(" ")[0]);
							if (Points < GambleAmount)
								Event.getChannel().send().message(DisplayName + ":  You don't have enough " + PointName + ".");
							else
								if (Gamblers.contains(User))
									Event.getChannel().send().message(DisplayName + ":  You have already joined.");
								else
								{
									Gamblers.add(User);
									int i = GambleAmount * Gamblers.size();
									Event.getChannel().send().message(DisplayName + " has joined the gamble for " + i + " total " + PointName + "!");
									try
									{
										RemovePoints(User, Event.getChannel(), GambleAmount);
									}
									catch (IOException e)
									{
										e.printStackTrace();
									}
								}
						}
					}
					catch (IOException | NumberFormatException | SQLException e)
					{
						e.printStackTrace();
					}
				}
				
				if (Event.getMessage().equalsIgnoreCase("!leaderboard") && PointChannels.contains(Event.getChannel().getName()))
				{
					try
					{
						HashMap<Integer, String> EntriesHash = new HashMap<Integer, String>();
						ArrayList<Integer> Entries = new ArrayList<Integer>();
						try
						{
							Statement State = Conn.createStatement();
							State.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "points`");
						}
						catch (SQLSyntaxErrorException e)
						{
							if (e.getMessage().equals("Table 'twitch." + Event.getChannel().getName().substring(1) + "points' doesn't exist"))
							{
								Statement State = Conn.createStatement();
								State.execute("CREATE TABLE `twitch`.`" + Event.getChannel().getName().substring(1) + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
							}
						}
						
						Statement State = Conn.createStatement();
						State.execute("SELECT * FROM `" + Event.getChannel().getName().substring(1) + "points`");
						ResultSet Results = State.getResultSet();
						while (Results.next())
						{
							EntriesHash.put(Integer.parseInt(Results.getString(2)), Results.getString(1));
							Entries.add(Integer.parseInt(Results.getString(2)));
						}
						
						int[] EntriesArr = new int[Entries.size()];
						for (int i = 0; i < Entries.size(); i++)
							EntriesArr[i] = Entries.get(i);
						Arrays.sort(EntriesArr);
						ReverseArray(EntriesArr);
						
						for (int i = 1; i < 6; i++)
							Event.getChannel().send().message("#" + i + ":  " + EntriesHash.get(EntriesArr[i-1]));
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!raid") && PointChannels.contains(Event.getChannel().getName()) && !Event.getChannel().getName().equals("#mangohberry"))
				{
					String Target = "";
					if (Event.getMessage().split(" ").length > 2 && Event.getMessage().split(" ")[1].equalsIgnoreCase("target"))
					{
						File Input = new File("Targets.txt");
						File Temp = new File("TempTargets.txt");
						BufferedReader Reader;
						PrintWriter Writer;
						String CurrentLine;
						
						try
						{
							Reader = new BufferedReader(new FileReader(Input));
							Writer = new PrintWriter(new FileOutputStream(Temp));
						}
						catch(FileNotFoundException e)
						{
							Event.getChannel().send().message(e.getLocalizedMessage());
							return;
						}
						
						try
						{
							while ((CurrentLine = Reader.readLine()) != null)
							{
								if (CurrentLine.split(Pattern.quote("~")).equals(Event.getChannel().getName()))
									continue;
								Writer.println(CurrentLine);
							}
						}
						catch (IOException e)
						{
							System.out.println(e.getStackTrace());
						}
						
						Writer.close();
						Input.delete();
						Temp.renameTo(Input);
						
						try
						{
							Writer = new PrintWriter(new FileOutputStream(Input));
						}
						catch (FileNotFoundException e)
						{
							Event.getChannel().send().message("Could not open file.");
							return;
						}
						Writer.println(Event.getChannel().getName() + "~" + Event.getMessage().substring(13));
						try
						{
							Reader.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
							return;
						}
						
						Event.getChannel().send().message("Our archers have their target, commander.");
						Writer.flush();
						Writer.close();
						return;
					}
					if (!OffCooldown("!raid"))
					{
						Event.getChannel().send().message(DisplayName + ":  Give them a minute to find another castle, okay?");
					}
					else
					{
						if (Event.getMessage().equalsIgnoreCase("!raid"))
						{
							if (Raiding)  // User joins the raid (if they have enough points and if they haven't already joined).
							{
								try
								{
									int Points = Integer.parseInt(GetPoints(User, Event.getChannel()).split(" ")[0]);
									if (Points < RaidAmount)
										Event.getChannel().send().message(DisplayName + ":  You don't have enough " + PointName + ".");
									else
									{
										if (Raiders.contains(User))
											Event.getChannel().send().message(DisplayName + ":  You have already joined the raid.");
										else
										{
											Raiders.add(User);
											RemovePoints(User, Event.getChannel(), RaidAmount);
											Event.getChannel().send().message(DisplayName + " has joined the raid!");
										}
									}
								}
								catch (IOException | NumberFormatException | SQLException e)
								{
									e.printStackTrace();
								}
							}
							else
								Event.getChannel().send().message(DisplayName + ":  A raid is not underway.  Type \"!raid [amount]\" to begin.");
						}
						else
						{
							try
							{
								if (Math.signum(Integer.parseInt(Event.getMessage().split(" ")[1])) != 1)
									Event.getChannel().send().message(DisplayName + ":  Number must be positive.");
								else 
								{
									if (Raiding)
										Event.getChannel().send().message(DisplayName + ":  There is already a raid underway.  Type \"!raid\" to join.");
									else
									{
										int Points = Integer.parseInt(GetPoints(User, Event.getChannel()).split(" ")[0]);
										if (Points < Integer.parseInt(Event.getMessage().split(" ")[1]))
											Event.getChannel().send().message(DisplayName + ":  You don't have enough " + PointName + ".");
										else // Begin a raid.
										{
											BufferedReader Reader =null;
											String CurrentLine;
											try
											{
												Reader = new BufferedReader(new FileReader("Targets.txt"));
											}
											catch (FileNotFoundException e)
											{
												e.printStackTrace();
												return;
											}
											try
											{
												while ((CurrentLine = Reader.readLine()) != null)
												{
													if (CurrentLine.split(Pattern.quote("~"))[0].equals(Event.getChannel().getName()))
														Target = CurrentLine.split(Pattern.quote("~"))[1];
												}
												if (Target.equals(""))
													Target = Event.getChannel().getName().substring(1) + "'s castle";
											}
											catch (IOException e)
											{
												e.printStackTrace();
											}
											
											Event.getChannel().send().message(DisplayName + " has started a raid on " + Target + " to claim their " + PointName + "!  Type \"!raid\" to join them!");
											try
											{
												RemovePoints(User, Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[1]));
											}
											catch (IOException e)
											{
												e.printStackTrace();
											}
											Raid(User, Event.getChannel(), Integer.parseInt(Event.getMessage().split(" ")[1]), Target);
										}
										
									}
									
								}
							}
							catch (NumberFormatException e)
							{
								Event.getChannel().send().message(DisplayName + ":  You must enter a number.");
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							catch (SQLException e1)
							{
								e1.printStackTrace();
							}
						}
					}
				}
				
				if (Event.getMessage().equalsIgnoreCase("!YateBotDiscord"))
					Event.getChannel().send().message("Click here to bring YateBot into your Discord server!  ->  http://discord.yatekko.com");
				
				
				/////////////////////////////////////////////////////////////////
				/////////////////  Custom Channel Functions  ///////////////////
				////////////////////////////////////////////////////////////////
				
				if (Event.getMessage().equalsIgnoreCase("!chance") && Event.getChannel().getName().equals("#rim99") && OffCooldown("!chance"))
				{
					Random Rand = new Random();
					int Result = Rand.nextInt(999) + 1;
					if (Result == 1000)  // 0.01%
						Event.getChannel().send().message(DisplayName + ":  Congratulations!  You won! p4ntzB");
					else
						if (Result > 950)  // 5%
							Event.getChannel().send().message(DisplayName + ":  So close!");
						else
							if (Result > 680)  // 27%
								Event.getChannel().send().message(DisplayName + ":  Getting there...");
							else
								if (Result > 400)  // 28%
									Event.getChannel().send().message(DisplayName + ":  Nice try.");
								else
									if (Result == 1) // 0.01%
									{
										Event.getChannel().send().message(DisplayName + ":  The gods have looked upon you and decided today is just not your day.  p4ntzREKT");
										Event.getChannel().send().message("/timeout " + DisplayName + " 180");
									}
									else  // 40%
										Event.getChannel().send().message(DisplayName + ":  Not even close. sbzyKappa");
					Cooldown.put("!chance", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
				}
				
				if (Event.getMessage().equalsIgnoreCase("!roulette") && !Roulette && (Event.getChannel().getName().equals("#rim99") || Event.getChannel().getName().equals("#tdlm1")) && OffCooldown("!roulette"))
				{
					//Roulette Bang = new Roulette(DisplayName, Event.getChannel(), Chambers, Event.getMessage());
					//Bang.run();
					Roulette(DisplayName, Event.getChannel(), Chambers, Event);
					Cooldown.put("!roulette", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!waifu") && Event.getChannel().getName().equals("#rim99"))
				{
					if (Event.getMessage().length() < 7)
					{
						Calendar LocalCalendar = Calendar.getInstance(TimeZone.getDefault());
						Random Rand = new Random();
						Rand.setSeed(LocalCalendar.get(Calendar.DATE));
						int Daily = Rand.nextInt(Waifus.size()-1);
						Event.getChannel().send().message("Today's waifu is " + Waifus.get(Daily) + ".");
					}
					else
					{
						if (Event.getMessage().split(" ")[1].equalsIgnoreCase("list"))
						{
							String Waifu = Waifus.get(0);
							if (Waifus.size() != 1)
							{
								for (int i = 1; i < Waifus.size(); i++)
								{
									Waifu = Waifu.concat(", " + Waifus.get(i));
								}
							}
							Event.getChannel().send().message(Waifu);
						}
						if (Event.getMessage().split(" ")[1].equalsIgnoreCase("add") && isMod(Event, User))
						{
							if (Event.getMessage().length() > 10)
							{
								try
								{
									Waifus.add(Event.getMessage().substring(11));
									PrintWriter Writer = new PrintWriter(new FileOutputStream("Waifu.txt", true));
									Writer.println(Event.getMessage().substring(11));
									Writer.flush();
									Writer.close();
									Event.getChannel().send().message("Waifu added.");
								}
								catch (Exception e)
								{
									Event.getChannel().send().message("Waifu could not be added.");
								}
							}
							else
								Event.getChannel().send().message(DisplayName + ":  You must specify a waifu.");
						}
						if (Event.getMessage().split(" ")[1].equalsIgnoreCase("remove") && isMod(Event, User))
						{
							if (Event.getMessage().length() > 13)
							{
								try
								{
									String Line = Event.getMessage().substring(14);
									String CurrentLine;
									boolean Found = false;
									File Input = new File("Waifu.txt");
									File Temp = new File("TempWaifu.txt");
									BufferedReader Reader = new BufferedReader(new FileReader(Input));
									PrintWriter Writer = new PrintWriter(new FileOutputStream(Temp), true);
									while ((CurrentLine = Reader.readLine()) != null)
									{
										System.out.println(CurrentLine);
										if (CurrentLine.equalsIgnoreCase(Line))
										{
											Found = true;
											Waifus.remove(Waifus.indexOf(CurrentLine));
											continue;
										}
										Writer.println(CurrentLine);
									}
									if (Found)
										Event.getChannel().send().message(DisplayName + ":  Waifu removed from list.");
									else
										Event.getChannel().send().message(DisplayName + ":  Waifu not found.");
									Reader.close();
									Writer.flush();
									Writer.close();
									Input.delete();
									Temp.renameTo(Input);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
							else
								Event.getChannel().send().message(DisplayName + ":  You must specify a waifu.");
						}
					}
				}
				
	
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!hug"))
				{
					if (Event.getMessage().equalsIgnoreCase("!hug"))
					{
						Event.getChannel().send().message(DisplayName + ":  Usage:  !hug [User]");
						return;
					}
					Event.getChannel().send().message(DisplayName + " touched " + Event.getMessage().substring(5) + "'s butt!  p4ntzB");
					return;
				}
				
				if (Event.getMessage().split(" ")[0].equalsIgnoreCase("!slap"))
				{
					if (Event.getMessage().equalsIgnoreCase("!slap"))
					{
						Event.getChannel().send().message(DisplayName + ":  Usage:  !slap [User]");
						return;
					}
					Event.getChannel().send().message(DisplayName + " slapped " + Event.getMessage().substring(6) + " with a large trout!");
					return;
				}
				
				if (Command.equals("!join") && Event.getChannel().getName().equals("#mangohberry"))
				{
					if (!OffCooldown("krystaljoin"))
					{
						Event.getChannel().send().message(DisplayName + ":  Please wait 3 minutes for the utensils to be cleaned.");
						return;
					}
					if (KrystalJoined == false)
					{
						Event.getChannel().send().message(DisplayName + " has started making poffins!  Type \"!join\" to join them!");
						KrystalJoin(User, Event.getChannel());
						return;
					}
					else
					{
						if (KrystalJoiners.contains(User))
						{
							Event.getChannel().send().message(DisplayName + ":  You have already joined!");
							return;
						}
						Event.getChannel().send().message(DisplayName + " has joined to help make poffins!");
						KrystalJoiners.add(User);
					}
				}
				
				if (Command.equals("!gamble") && Event.getChannel().getName().equals("#mangohberry"))
				{
					if (KrystalGambling)
						return;
					if (!OffCooldown("KrystalGamble"))
					{
						Event.getChannel().send().message(DisplayName + ":  Arceus does not want to be bothered right now.  Try again in 1 minute.");
						return;
					}
					try
					{
						if (Math.signum(Integer.parseInt(Event.getMessage().split(" ")[1])) != 1)
						{
							Event.getChannel().send().message(DisplayName + ":  Number must be positive.");
							return;
						}
						int Points = Integer.parseInt(GetPoints(User, Event.getChannel()).split(" ")[0]);
						int KrystalGambleAmount = Integer.parseInt(Event.getMessage().split(" ")[1]);
						if (Points < KrystalGambleAmount)
						{
							Event.getChannel().send().message(DisplayName + ":  You don't have enough " + PointName + ".");
							return;
						}
						RemovePoints(User, Event.getChannel(), KrystalGambleAmount);
						KrystalGamble(User, Event.getChannel(), KrystalGambleAmount);
						return;
					}
					catch (NumberFormatException e)
					{
						Event.getChannel().send().message(DisplayName + ":  You can only enter a whole number.");
						return;
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
					
				}
				
				if (Command.equalsIgnoreCase("!drink") && Event.getChannel().getName().equals("#rim99"))
				{
					try
					{
						if (Event.getMessage().equalsIgnoreCase("!drink"))
						{
							Statement State = Conn.createStatement();
							State.execute("SELECT * FROM `rimdrinks`");
							ResultSet Result = State.getResultSet();
							Result.next();
							int Drinks = Result.getInt(1);
							Event.getChannel().send().message("Drinks Finished:  " + String.valueOf(Drinks));
							return;
						}
						if (Args[0].equalsIgnoreCase("add") && isMod(Event, User))
						{
							Statement State = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
							State.execute("SELECT * FROM `rimdrinks`");
							ResultSet Result = State.getResultSet();
							Result.next();
							int Drinks = Result.getInt(1);
							Drinks++;
							Result.updateInt(1, Drinks);
							Result.updateRow();
							Event.getChannel().send().message("Drinks Finished:  " + String.valueOf(Drinks));
							return;
						}
						if (Args[0].equalsIgnoreCase("remove") && isMod(Event, User))
						{
							Statement State = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
							State.execute("SELECT * FROM `rimdrinks`");
							ResultSet Result = State.getResultSet();
							Result.next();
							int Drinks = Result.getInt(1);
							Drinks--;
							Result.updateInt(1, Drinks);
							Result.updateRow();
							Event.getChannel().send().message("Drinks Finished:  " + String.valueOf(Drinks));
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				
				if (Command.equals("!tickets") && Event.getChannel().getName().equals("#rim99"))
				{
					if (Args != null && Args.length == 1 && isInteger(Args[0]))
					{
						int Points;
						Statement State;
						ResultSet Result;
						int Tickets;
						try
						{
							State = Conn.createStatement();
							State.execute("SELECT * FROM `rim99points` WHERE `Username` = '" + User + "'");
							Result = State.getResultSet();
							Result.next();
							Points = Result.getInt("Points");
							
							
						}
						catch (SQLException e)
						{
							Event.getChannel().send().message(DisplayName + ":  You have no Rimbits.");
							return;
						}
						try
						{
							State.execute("SELECT * FROM `rim99tickets` WHERE `Username` = '" + User + "'");
							Result = State.getResultSet();
							Result.next();
							Tickets = Result.getInt("Tickets");
							Tickets += Integer.parseInt(Args[0]);
						}
						catch (SQLException e2)
						{
							Tickets = 0;
						}
						if (Points < 2500*Integer.parseInt(Args[0]))
						{
							Event.getChannel().send().message(DisplayName + ":  You don't have enough Rimbits.");
							return;
						}
						try
						{
							State.execute("INSERT INTO `rim99tickets` (`Username`, `Tickets`) VALUES ('" + User + "', '" + Integer.parseInt(Args[0]) + "')");
							Event.getChannel().send().message(DisplayName + " bought " + Args[0] + " tickets.");
							RemovePoints(User, Event.getChannel(), 2500*Integer.parseInt(Args[0]));
							return;
						}
						catch (SQLIntegrityConstraintViolationException e)
						{
							try
							{
								State.execute("UPDATE `rim99tickets` SET `Tickets` = '" + Tickets + "' WHERE `Username` = '" + User + "'");
								Event.getChannel().send().message(DisplayName + " bought " + (Tickets - Integer.parseInt(Args[0])) + " tickets for a total of " + Tickets + " tickets.");
								RemovePoints(User, Event.getChannel(), 2500*Integer.parseInt(Args[0]));
								return;
							}
							catch (SQLException | NumberFormatException | IOException e1)
							{
								e.printStackTrace();
								return;
							}
						}
						catch (SQLException | NumberFormatException | IOException e)
						{
							e.printStackTrace();
							return;
						}
					}
					try
					{
						Statement State = Conn.createStatement();
						State.execute("SELECT * FROM `rim99tickets` WHERE `Username` = '" + User + "'");
						ResultSet Result = State.getResultSet();
						Result.next();
						int Tickets = Result.getInt("Tickets");
						Event.getChannel().send().message(DisplayName + ":  " + Tickets + " ticket(s).");
						return;
					}
					catch (SQLException e2)
					{
						Event.getChannel().send().message(DisplayName + ":  0 tickets.");
						return;
					}
				}
			}
		});
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
			Conn = DriverManager.getConnection(TwitchDB);
			Conn.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		//Twitter.setOAuthConsumer("JVDUcUNhuHrb0jDqD7G3GW0iW", "2WzpE7eO3QfWoatgTYSCxBwCSx6rmKKxLrdhjAva0ecSgT7taz");
		
		

		// Get access tokens from server
		String User;
		String Token;
		String Secret;
		long ID;
		
		Statement State = Conn.createStatement();
		State.execute("SELECT * FROM `twitter` WHERE 1");
		ResultSet Result = State.getResultSet();
		
		while (Result.next())
		{
			User = Result.getString(1);
			Token = Result.getString(2);
			Secret = Result.getString(3);
			ID = Long.parseLong(Result.getString(4));
			TwitterToken.put(User, Token);
			TwitterSecret.put(User, Secret);
			TwitterID.put(User, ID);
		}

		// Set waifus
		try {
			String CurrentLine;
			BufferedReader Reader = new BufferedReader(new FileReader("Waifu.txt"));
			while ((CurrentLine = Reader.readLine()) != null)
				Waifus.add(CurrentLine);
			Reader.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		// Set polls
		try
		{
			State.execute("SELECT * FROM `polls`");
			Result = State.getResultSet();
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
	}

	public void onUnknown(String Event)
	{
		
		if (Event.startsWith("@badges"))
		{
			/////////////////////////////////////////////////////////
			/////////////////   WHISPERS   /////////////////////////
			////////////////////////////////////////////////////////
			/*if (Event.contains("tmi.twitch.tv WHISPER"))
			{
				String MessageChunk = Event.substring(Event.indexOf("WHISPER"));
				String User = Event.substring(Event.indexOf(":")+1,  Event.indexOf("!"));
				String Event.getMessage() = MessageChunk.substring(MessageChunk.indexOf(":")+1);
				Twitter Twitter = TwitterFactory.getSingleton();
				RequestToken RT = null;
				Twitter.setOAuthConsumer("JVDUcUNhuHrb0jDqD7G3GW0iW", "2WzpE7eO3QfWoatgTYSCxBwCSx6rmKKxLrdhjAva0ecSgT7taz");
				
				if (Event.getMessage().equalsIgnoreCase("!tweet"))
				{
					if (RT == null)
					{
						try 
						{
							RT = Twitter.getOAuthRequestToken();
						} catch (TwitterException e) 
						{
							e.printStackTrace();
						}
					}	
					this.sendRawLineViaQueue("PRIVMSG #jtv :/w " + User + " Follow the link below and authenticate YateBot to post on your behalf.  You will then be redirected.  When you reach YateBot's documentation, copy the URL and whisper it to YateBot.  I'm sorry I couldn't make this easier!  >.<");
					this.sendRawLineViaQueue("PRIVMSG #jtv :/w " + User + " " + RT.getAuthenticationURL());
				}
				else
				{
					try
					{
						AccessToken AT = null;
						int UrlLength = Event.getMessage().split(Pattern.quote("=")).length;
						String Pin = null;
						if (Event.getMessage().startsWith("https://sites.google.com/site/yatebotdocs/?oauth_token=") && UrlLength == 3)
							Pin = Event.getMessage().split(Pattern.quote("="))[2];
						else
							throw new MalformedURLException();
						AT = Twitter.getOAuthAccessToken(RT, Pin);
						
						
						Statement State = Conn.createStatement();
						State.execute("INSERT INTO `twitter`(`Name`, `AccessToken`, `Secret`, `ID`) VALUES ('" + User + "','" +  AT.getToken() + "','" + AT.getTokenSecret() + "','" +AT.getUserId() + "')");
						
						TwitterToken.put(User, AT.getToken());
						TwitterSecret.put(User, AT.getTokenSecret());
						TwitterID.put(User, AT.getUserId());
						this.sendRawLineViaQueue("PRIVMSG #jtv :/w " + User + " Successfully authorized!");
					}
					catch(MalformedURLException e)
					{
						this.sendRawLineViaQueue("PRIVMSG #jtv :/w " + User + " Authorization token could not be detected.");
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
					catch(Exception e)
					{
						this.sendRawLineViaQueue("PRIVMSG #jtv :/w " + User + " Failed to authorize.");
					}
				}
			}*/
			
			////////////////////////////////////////////////////////////
			/////////////////////   NORMAL CHAT   /////////////////////
			///////////////////////////////////////////////////////////
			/*if (Event.contains(":tmi.twitch.tv USERSTATE") || Event.contains("tmi.twitch.tv WHISPER"))
			{
				String MessageChunk = "";
			}
			else
			{*/
		}
				
	}

	////////////////////////////////////////////////////
	////////////// Helper Functions /////////////////
	///////////////////////////////////////////////////

	@SuppressWarnings("resource")
	public void AddCom(String Message, Channel Channel, String DisplayName, boolean UserCalled)
	{
		String Table = Channel.getName().substring(1);
		if (Message.length() <= 7 || (!Message.split(" ")[1].startsWith("!") && !Message.split(" ")[1].equals("-M")))
			Channel.send().message(DisplayName + ":  Usage:  !addcom (-M if Mod-Only) ![command name] [output]");
		else {
			Statement State = null;
			try 
			{
				State = Conn.createStatement();
				State.execute("SELECT * FROM `" + Table + "` WHERE `Name` = '" + Message.split(" ")[1] + "'");
				ResultSet Result = State.getResultSet();
				if (Result.next())
				{
					Channel.send().message(DisplayName + ":  Command already exists.");
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
								Channel.send().message(DisplayName + ":  Command " + Message.split(" ")[2] + " added.");
							State.close();
							return;
						}
						else
						{
							State.execute("INSERT INTO `" + Table + "`(`Name`, `Command`, `Mod`) VALUES ('" + Message.split(" ")[1] + "','" + Command + "','0')");
							if (UserCalled)
								Channel.send().message(DisplayName + ":  Command " + Message.split(" ")[1] + " added.");
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

	public boolean DelCom(String Message, Channel Channel, String DisplayName, boolean UserCalled)
	{
		if (Message.length() <= 7 || (!Message.split(" ")[1].startsWith("!") && !Message.split(" ")[1].equals("-M")))
		{
			Channel.send().message(DisplayName + ":  Usage:  !delcom ![command name]");
			return false;
		}
		else 
		{
			try
			{
				String Table = Channel.getName().substring(1);
				if (Message.split(" ")[0].equalsIgnoreCase("!prizes") || Message.split(" ")[0].equalsIgnoreCase("!fame"))
				{
					if (Channel.equals("#rim99"))
						Table = "rim99";
				}
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
							Channel.send().message(DisplayName + ":  Command " + Message.split(" ")[1] + " deleted.");
						return true;
					}
					else
					{
						if (UserCalled)
							Channel.send().message(DisplayName + ":  Command " + Message.split(" ")[1] + " not found.");
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

	public void AddQuote(String Message, Channel Channel, String DisplayName, boolean UserCalled)
	{
		try 
		{
			String Game;
			URL Strim;
			Strim = new URL("https://api.twitch.tv/kraken/channels/" + Channel.getName().substring(1));
			URLConnection Connect = Strim.openConnection();
			Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
			InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
			Page Items = new Gson().fromJson(Reader, Page.class);
			Game = Items.game;
			DateFormat DF = new SimpleDateFormat("MM/dd/yyyy");
			Date Today = new Date();
			String SQLMessage = Message;
			
			if (Message.contains("'"))
			{
				SQLMessage = "";
				String[] Chunks = Message.split(Pattern.quote("'"));
				for (int i = 0; i < Chunks.length; i++)
					Chunks[i] = Chunks[i].concat("\\'");
				for (int i = 0; i < Chunks.length; i++)
					SQLMessage = SQLMessage.concat(Chunks[i]);
				SQLMessage = SQLMessage.substring(0, SQLMessage.length()-2);
			}
			
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "quotes` ORDER BY `" + Channel.getName().substring(1) + "quotes`.`Index` ASC");
			ResultSet Result = State.getResultSet();
			Result.last();
			int RowNum = Result.getRow()+1;
			State.execute("INSERT INTO `" + Channel.getName().substring(1) + "quotes` (`Index`, `Quote`) VALUES ('" + RowNum + "','" + SQLMessage + " [" + Game + " - " + DF.format(Today) + "]')");
			//State.execute("INSERT INTO `" + Channel.substring(1) + "quotes` (`Quote`) VALUES ('" + Message + " [" + Game + " - " + DF.format(Today) + "]')");
			if (UserCalled)
				Channel.send().message(DisplayName + ":  Quote " + RowNum + " added.");
			return;
		}
		catch (SQLException | IOException e)
		{
			e.printStackTrace();
			return;
		}
	}

	public boolean DelQuote(String Message, Channel Channel, String DisplayName, boolean UserCalled, String Event) throws IOException
	{
		try
		{
			Statement State = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "quotes` ORDER BY `" + Channel.getName().substring(1) + "quotes`.`Index` ASC");
			ResultSet Result = State.getResultSet();
			Result.absolute(Integer.parseInt(Message.split(" ")[1]));
			int Row = Result.getRow();
			if (Row != Integer.parseInt(Message.split(" ")[1]))
			{
				Channel.send().message(DisplayName + ":  Quote #" + Message.split(" ")[1] + " not found.");
				return false;
			}
			//State.execute("DELETE FROM `" + Channel.substring(1) + "quotes` WHERE `QuoteNumber` = '" + Row + "'");
			Result.deleteRow();
			if (UserCalled)
				Channel.send().message(DisplayName + ":  Quote #" + Row + " deleted.");
			UpdateTable("twitch", Channel.getName().substring(1) + "quotes");
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean isMod(MessageEvent Event, String User)
	{
		boolean Mod = false;
		
		
		String Perms[] = { "broadcaster", "staff", "admin", "global_mod", "moderator" };

		if (User.equals("yatekko"))
			Mod = true;
		for (int i = 0; i <= 4 && !Mod; i++) {
			if (Event.getTags().get("badges").contains(Perms[i]))
				Mod = true;
		}

		

		return Mod;
	}

	public boolean isBroadcaster(MessageEvent Event, String User)
	{
		boolean Broad = false;

		if (User.equals("yatekko"))
			Broad = true;
		if (Event.getTags().get("badges").contains("broadcaster"))
			Broad = true;

		return Broad;
	}

	public boolean OffCooldown(String Key)
	{
		if (Cooldown.containsKey(Key)) 
		{
			if ((Cooldown.get(Key) + 10) < (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
				return true;
			else
				return false;
		}
		return true;
	}
	
	public boolean KrystalOffCooldown(String Key)
	{
		if (KrystalGambleCooldown.containsKey(Key)) 
		{
			if ((KrystalGambleCooldown.get(Key) + 180) < (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
				return true;
			else
				return false;
		}
		return true;
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

	public String Uptime(Channel Channel) throws IOException
	{
		URL Strim;
		Strim = new URL("https://api.rtainc.co/twitch/uptime?channel=" + Channel.getName().substring(1));
		URLConnection Connect = Strim.openConnection();
		BufferedReader Reader = null;;
		try
		{
			Reader = new BufferedReader(new InputStreamReader(Connect.getInputStream()));
		}
		catch (IOException e)
		{
			System.out.println("Exception caught.");
		}
		
		String Time = Reader.readLine();
		try
		{
		if (Time.startsWith(Channel.getName().substring(1)))
			return Time;
		else
			return "Stream has been live for " + Time + ".";
		}
		catch (NullPointerException e)
		{
			return null;
		}
	}

	public void Game(String Message, Channel Channel, String DisplayName) throws IOException
	{
		URL Strim;
		Strim = new URL("https://api.twitch.tv/kraken/channels/" + Channel.getName().substring(1));
		URLConnection Connect = Strim.openConnection();
		Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
		InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
		Page Items = new Gson().fromJson(Reader, Page.class);
		Channel.send().message(DisplayName + ":  " + Items.game);
	}

	public void Title(String Message, Channel Channel, String DisplayName) throws IOException
	{
		URL Strim;
		Strim = new URL("https://api.twitch.tv/kraken/channels/" + Channel.getName().substring(1));
		URLConnection Connect = Strim.openConnection();
		Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
		InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
		Page Items = new Gson().fromJson(Reader, Page.class);
		Channel.send().message(DisplayName + ":  " + Items.status);
	}

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

	public void StrawpollResults(Channel Channel) throws Exception
	{
		HttpClient Client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost Post = new HttpPost("https://strawpoll.me/api/v2/polls/" + StrawpollLink.get(Channel.getName()));
		StringEntity PostingString = new StringEntity(
				"https://strawpoll.me/api/v2/polls/" + StrawpollLink.get(Channel.getName()));
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
			Channel.send().message("There have been no votes yet.");
		else if (TieNum > 1) {
			String Message = "There is a tie between ";
			for (int i = 0; i < Results.votes.length; i++) {
				if (Ties[i])
					Message = Message.concat("\"" + Results.options[i] + "\"" + ", ");
			}
			Message = Message.substring(0, Message.length() - 2);
			Message = Message.concat("!");
			Channel.send().message(Message);
		} else {
			String Plural = "";
			if (WinnerVotes > 1)
				Plural = "s";
			Channel.send().message("\"" + Winner + "\"" + " is currently winning with " + WinnerVotes + " vote" + Plural + "!");
		}
	}

	public boolean isLive(Channel Channel) throws IOException
	{
		boolean Result = true;
		String Live = Uptime(Channel);
		if (Live.startsWith(Channel.getName().substring(1)))
			Result = false;
		return Result;
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
	
	public void UpdateTable(String Database, String Table)
	{
		Connection Conner;
		try
		{
			if (Database.equals("twitch"))
				Conner = DriverManager.getConnection(TwitchDB);
			else
				Conner = DriverManager.getConnection(DiscordDB);
			Conner.setAutoCommit(true);
			
			Statement State = Conner.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			State.execute("SELECT * FROM `" + Table + "`");
			ResultSet Result = State.getResultSet();
			int i = 1;
			while(Result.next())
			{
				Result.updateInt(1, i);
				Result.updateRow();
				i++;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public Channel GetChannel()
	{
		Channel Chan = this.getUserBot().getChannels().first();
		return Chan;
	}
	
	/*public Channel GetChannel(String Chan)
	{
		Channel ReturnChan;
		return ReturnChan;
	}*/
	
	public void ChangeNick(String Nickname)
	{
		this.setNick(Nickname);
	}

	///////////////////
	// Points System //
	///////////////////

	public void PointUpdate(Channel Channel) throws IOException, SQLException
	{
		try
		{
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
		}
		catch (SQLSyntaxErrorException e)
		{
			if (e.getMessage().equals("Table 'twitch." + Channel.getName().substring(1) + "points' doesn't exist"))
			{
				Statement State = Conn.createStatement();
				State.execute("CREATE TABLE `twitch`.`" + Channel.getName().substring(1) + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
			}
		}
		
		Statement State;
		ResultSet Result;
		HashMap<String, String> Chatters = new HashMap<String, String>();
		try
		{
			State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
			Result = State.getResultSet();
			while (Result.next())
				Chatters.put(Result.getString(1), Result.getString(2));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		URL Strim = new URL("https://tmi.twitch.tv/group/user/" + Channel.getName().substring(1) + "/chatters");
		URLConnection Connect = Strim.openConnection();
		InputStreamReader URLReader = new InputStreamReader(Connect.getInputStream());
		Stream Users = new Gson().fromJson(URLReader, Stream.class);

		for (int i = 0; i < Users.chatters.admins.length; i++) {
			if (Chatters.containsKey(Users.chatters.admins[i])) {
				int j = Integer.parseInt(Chatters.get(Users.chatters.admins[i]));
				j += 50;
				Chatters.put(Users.chatters.admins[i], String.valueOf(j));
			} else
				Chatters.put(Users.chatters.admins[i], String.valueOf(50));
		}
		for (int i = 0; i < Users.chatters.global_mods.length; i++) {
			if (Chatters.containsKey(Users.chatters.global_mods[i])) {
				int j = Integer.parseInt(Chatters.get(Users.chatters.global_mods[i]));
				j += 50;
				Chatters.put(Users.chatters.global_mods[i], String.valueOf(j));
			} else
				Chatters.put(Users.chatters.global_mods[i], String.valueOf(50));
		}
		for (int i = 0; i < Users.chatters.moderators.length; i++) {
			if (Chatters.containsKey(Users.chatters.moderators[i])) {
				int j = Integer.parseInt(Chatters.get(Users.chatters.moderators[i]));
				j += 50;
				Chatters.put(Users.chatters.moderators[i], String.valueOf(j));
			} else
				Chatters.put(Users.chatters.moderators[i], String.valueOf(50));
		}
		for (int i = 0; i < Users.chatters.staff.length; i++) {
			if (Chatters.containsKey(Users.chatters.staff[i])) {
				int j = Integer.parseInt(Chatters.get(Users.chatters.staff[i]));
				j += 50;
				Chatters.put(Users.chatters.staff[i], String.valueOf(j));
			} else
				Chatters.put(Users.chatters.staff[i], String.valueOf(50));
		}
		for (int i = 0; i < Users.chatters.viewers.length; i++) {
			if (Chatters.containsKey(Users.chatters.viewers[i])) {
				int j = Integer.parseInt(Chatters.get(Users.chatters.viewers[i]));
				j += 50;
				Chatters.put(Users.chatters.viewers[i], String.valueOf(j));
			} else
				Chatters.put(Users.chatters.viewers[i], String.valueOf(50));
		}
		
		if (Chatters.containsKey("yatebot"))
			Chatters.put("yatebot", String.valueOf(0));
		
		
		for (Map.Entry<String, String> Entry : Chatters.entrySet())
		{	
				State = Conn.createStatement();
				try
				{
					State.execute("INSERT INTO `" + Channel.getName().substring(1) + "points` (`Username`, `Points`) VALUES ('" + Entry.getKey() + "', '" + Entry.getValue() + "')");
				}
				catch (SQLIntegrityConstraintViolationException e)
				{
					State.execute("UPDATE `" + Channel.getName().substring(1) + "points` SET `Username`='" + Entry.getKey() + "',`Points`='" + Entry.getValue() + "' WHERE `Username`='" + Entry.getKey() + "'");
				}
		}
	}

	public String AddPoints(String User, Channel Channel, int Amount) throws IOException, SQLException
	{
		try
		{
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
		}
		catch (SQLSyntaxErrorException e)
		{
			if (e.getMessage().equals("Table 'twitch." + Channel.getName().substring(1) + "points' doesn't exist"))
			{
				Statement State = Conn.createStatement();
				State.execute("CREATE TABLE `twitch`.`" + Channel.getName().substring(1) + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
			}
		}
		
		try {
			URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + User);
			URLConnection Connect = Strim.openConnection();
			Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
			InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
			Page Items = new Gson().fromJson(Reader, Page.class);
		} catch (FileNotFoundException e) {
			return "User not found. Perhaps try spelling it properly. p4ntzREKT";
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Statement State;
		ResultSet Result;
		HashMap<String, String> Chatters = new HashMap<String, String>();
		try
		{
			State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
			Result = State.getResultSet();
			while (Result.next())
				Chatters.put(Result.getString(1), Result.getString(2));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		if (Chatters.containsKey(User)) {
			int j = Integer.parseInt(Chatters.get(User));
			j += Amount;
			Chatters.put(User, String.valueOf(j));
		} else
			Chatters.put(User, String.valueOf(Amount));

		State = Conn.createStatement();
		try
		{
			State.execute("INSERT INTO `" + Channel.getName().substring(1) + "points` (`Username`, `Points`) VALUES ('" + User + "', '" + Chatters.get(User) + "')");
		}
		catch (SQLIntegrityConstraintViolationException e)
		{
			State.execute("UPDATE `" + Channel.getName().substring(1) + "points` SET `Username`='" + User + "',`Points`='" + Chatters.get(User) + "' WHERE `Username`='" + User + "'");
		}

		return "Added " + Amount + " " + PointName + " to " + User;
	}

	public String RemovePoints(String User, Channel Channel, int Amount) throws IOException, SQLException
	{
		try
		{
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
		}
		catch (SQLSyntaxErrorException e)
		{
			if (e.getMessage().equals("Table 'twitch." + Channel.getName().substring(1) + "points' doesn't exist"))
			{
				Statement State = Conn.createStatement();
				State.execute("CREATE TABLE `twitch`.`" + Channel.getName().substring(1) + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
			}
		}
		
		int Removed = 0;
		try {
			URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + User);
			URLConnection Connect = Strim.openConnection();
			Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
			InputStreamReader Reader = new InputStreamReader(Connect.getInputStream());
			Page Items = new Gson().fromJson(Reader, Page.class);
		} catch (FileNotFoundException e) {
			return "User not found. Perhaps try spelling it properly. p4ntzREKT";
		} catch (IOException e) {
			e.printStackTrace();
		}

		Statement State;
		ResultSet Result;
		HashMap<String, String> Chatters = new HashMap<String, String>();
		try
		{
			State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
			Result = State.getResultSet();
			while (Result.next())
				Chatters.put(Result.getString(1), Result.getString(2));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		if (Chatters.containsKey(User)) {
			int UserPoints = Integer.parseInt(Chatters.get(User));
			if ((UserPoints - Amount) < 0) {
				Removed = Amount - (Amount - UserPoints);
				UserPoints = 0;
			} else {
				UserPoints -= Amount;
				Removed = Amount;
			}
			Chatters.put(User, String.valueOf(UserPoints));
		} else
			Chatters.put(User, String.valueOf(Amount));

		State = Conn.createStatement();
		State.execute("UPDATE `" + Channel.getName().substring(1) + "points` SET `Username`='" + User + "',`Points`='" + Chatters.get(User) + "' WHERE `Username`='" + User + "'");

		return "Removed " + Removed + " " + PointName + " from " + User;
	}

	public String GetPoints(String User, Channel Channel) throws IOException, SQLException
	{
		try
		{
			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points`");
		}
		catch (SQLSyntaxErrorException e)
		{
			if (e.getMessage().equals("Table 'twitch." + Channel.getName().substring(1) + "points' doesn't exist"))
			{
				Statement State = Conn.createStatement();
				State.execute("CREATE TABLE `twitch`.`" + Channel.getName().substring(1) + "points` ( `Username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , `Points` INT(255) NOT NULL , PRIMARY KEY (`Username`)) ENGINE = InnoDB;");
			}
		}
		
		try 
		{
			URL Strim = new URL("https://api.twitch.tv/kraken/channels/" + User);
			URLConnection Connect = Strim.openConnection();
			Connect.setRequestProperty("Client-ID", "rbxt0l8im6pnbz7k26dydt0m4gxij1d");
			InputStreamReader IRReader = new InputStreamReader(Connect.getInputStream());
			Page Items = new Gson().fromJson(IRReader, Page.class);

			Statement State = Conn.createStatement();
			State.execute("SELECT * FROM `" + Channel.getName().substring(1) + "points` WHERE `Username` = '" + User.toLowerCase() + "'");
			ResultSet Result = State.getResultSet();
			Result.next();
			String ResultString = Result.getString(2) + " " + PointName + ".";
			return ResultString;
		}
		catch (FileNotFoundException e) 
		{
			return "User not found. Perhaps try spelling it properly. p4ntzREKT";
		} catch (IOException e) 
		{
			if (e.getMessage().contains("response code: 422"))
			{
				return "User not found. Perhaps try spelling it properly. p4ntzREKT";
			}
			e.printStackTrace();
			return "";
		}
		catch (SQLException e)
		{
			return "0 " + PointName + ".";
		}
	}
	
	public void Gamble(String User, final Channel Channel, final int Amount)
	{
		Gambling = true;
		Gamblers.add(User);
		GambleAmount = Amount;
		int Delay = 30000;
		final Timer Time = new Timer(Delay, null);
		Time.addActionListener(new ActionListener()
		//ActionListener ActionGambling = new ActionListener()
		{
			public void actionPerformed(ActionEvent Event)
			{
				Random Rand = new Random();
				int Result = Rand.nextInt(Gamblers.size());
				String Winner = Gamblers.get(Result);
				int Pot = Amount * Gamblers.size();
				
				try
				{
					AddPoints(Winner, Channel, Pot);
					Gamblers.clear();
					GambleAmount = 0;
					Gambling = false;
				}
				catch (IOException | SQLException e)
				{
					e.printStackTrace();
				}
				Channel.send().message("Congratulations " + Winner + "!  You won " + String.valueOf(Pot) + " " + PointName + "!");
			}
		});
		//Timer T = new Timer(Delay, ActionGambling);
		//T.setRepeats(false);
		//T.start();
		Time.setRepeats(false);
		Time.start();
	}
	
	public void Raid(String User, final Channel Channel, final int Amount, final String Target)
	{
		Raiding = true;
		Raiders.add(User);
		RaidAmount = Amount;
		int Delay = 30000;
		ActionListener ActionRaiding = new ActionListener()
		{
			public void actionPerformed(ActionEvent Event)
			{
				double Multiplier;
				int SuccessRate;
				
				if (Raiders.size() == 1)
				{
					Multiplier = 1.35;
					SuccessRate = 60;
				}
				else
					if (Raiders.size() < 5)
					{
						Multiplier = 1.5;
						SuccessRate = 53;
					}
					else
						if (Raiders.size() < 10)
						{
							Multiplier = 2;
							SuccessRate = 46;
						}
						else
							if (Raiders.size() < 20)
							{
								Multiplier = 2.35;
								SuccessRate = 39;
							}
							else
								if (Raiders.size() < 30)
								{
									Multiplier = 2.75;
									SuccessRate = 32;
								}
								else
								{
									Multiplier = 3.25;
									SuccessRate = 25;
								}
				
				Random Rand = new Random();
				int Result = Rand.nextInt(100)+1;
				if (Result > SuccessRate)
				{
					int Payout =  (int) (Amount * Multiplier);
					for (int i = 0; i < Raiders.size(); i++)
					{
						try 
						{
							AddPoints(Raiders.get(i), Channel, Payout);
						} 
						catch (IOException | SQLException e) 
						{
							e.printStackTrace();
						}
					}
					Raiders.clear();
					Raiding = false;
					RaidAmount = 0;
					Cooldown.put("!raid", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+50);
					Channel.send().message("The raiders were successful in taking " + Target + " and looted " + String.valueOf(Payout) + " " + PointName + "!");
				}
				else
				{
					Raiders.clear();
					Raiding = false;
					RaidAmount = 0;
					Cooldown.put("!raid", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+50);
					Channel.send().message("Unfortunately, the raiders were repelled by the defenders of " + Target + "...");
				}
			}
		};
		Timer T = new Timer(Delay, ActionRaiding);
		T.setRepeats(false);
		T.start();
	}
	
	public void KrystalJoin(String User, Channel Channel)
	{
		KrystalJoined = true;
		KrystalJoiners.add(User);
		int Delay = 30000;
		ActionListener ActionJoin = new ActionListener()
		{
			public void actionPerformed(ActionEvent Event)
			{
				String PluralCheck;
				if (KrystalJoiners.size() == 1)
					PluralCheck = "Poffin was";
				else
					PluralCheck = "Poffins were";
				for (int i = 0; i < KrystalJoiners.size(); i++)
				{
					try
					{
						AddPoints(KrystalJoiners.get(i), Channel, KrystalJoiners.size());
					}
					catch (IOException | SQLException e)
					{
						e.printStackTrace();
					}
				}
				KrystalJoined = false;
				Channel.send().message(KrystalJoiners.size() + " " + PluralCheck + " made for everyone who joined!");
				KrystalJoiners.clear();
				Cooldown.put("krystaljoin", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+170);
			}
		};
		Timer T = new Timer(Delay, ActionJoin);
		T.setRepeats(false);
		T.start();
	}
	
	public void KrystalGamble(final String User, Channel Channel, final int Amount)
	{
		KrystalGambling = true;
		int Delay = 10000;
		Channel.send().message("Arceus gazes at your offering...");
		ActionListener ActionGamble = new ActionListener()
		{
			public void actionPerformed(ActionEvent Event)
			{
				try
				{
					Random Rand = new Random();
					if (Rand.nextInt(2) == 1)
					{
						Channel.send().message("Arceus is pleased with your offering!  You receive " +  Amount*2 + " Poffins as your reward!");
						AddPoints(User, Channel, Amount*2);
						Cooldown.put("KrystalGamble", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+50);
						KrystalGambling = false;
						return;
					}
					else
					{
						Channel.send().message("Arceus does not look happy... you had better get out of there while you can!");
						Cooldown.put("KrystalGamble", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+50);
						KrystalGambling = false;
						return;
					}
				}
				catch (SQLException | IOException e)
				{
					e.printStackTrace();
					return;
				}
			}
		};
		Timer T = new Timer(Delay, ActionGamble);
		T.setRepeats(false);
		T.start();
	}
	
	public void KrystalMoo()
	{
		if (!(GetChannel().getName().equals("#mangohberry")))
			return;
		ActionListener ActionMoo = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (!isLive(GetChannel()))
						return;
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
					return;
				}
				Random Rand = new Random();
				if (Rand.nextInt(10) == 1)
					GetChannel().send().message("/me Festive Moo.");
				return;
			}
		};
		Timer T = new Timer(300000, ActionMoo);
		T.start();
	}
	
	public void RimMoo()
	{
		if (!(GetChannel().getName().equals("#rim99")))
			return;
		ActionListener ActionMoo = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (!isLive(GetChannel()))
						return;
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
					return;
				}
				Random Rand = new Random();
				if (Rand.nextInt(10) == 1)
					GetChannel().send().message("/me Festive Moo.");
				return;
			}
		};
		Timer T = new Timer(300000, ActionMoo);
		T.start();
	}
	
	public void Roulette(String User, Channel Channel, HashMap<String, Integer> Chambers, MessageEvent Event)
	{
		
		Roulette = true;
		int Delay = 4000;
		Channel.send().message(User + " pulls the trigger...");
		final Timer Time = new Timer(Delay, null);
		Time.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent Ev)
			{
				try
				{
					if (!Chambers.containsKey(Channel.getName()))
						Chambers.put(Channel.getName(), 8);
					Random Rand = new Random();
					int Result = Rand.nextInt(Chambers.get(Channel.getName()));
					if (Result == 0) 
					{
						Channel.send().message("BANG!");
						Chambers.put(Channel.getName(), 8);
						if (isBroadcaster(Event, User))
						{
							Channel.send().message("However, the bullet was stopped by " + User + "'s Shield of Divinity +2!");
							Roulette = false;
							return;
						}
						if (isMod(Event, User))
						{
							Channel.send().message("However, the bullet was stopped by " + User + "'s Sword of Moderation +1!");
							Roulette = false;
							return;
						}
						Channel.send().message("/timeout " + User + " 180");
						Roulette = false;
						return;
					} 
					else 
					{
						Channel.send().message("Click!");
						System.out.println("Chambers:  " + Chambers.get(Channel.getName()));
						System.out.println("Rolled:  " + Result);
						Chambers.put(Channel.getName(), Chambers.get(Channel.getName()) - 1);
						Roulette = false;
					}
				}
				catch (Exception e)
				{
					Channel.send().message("An error occurred.");
					e.printStackTrace();
					Roulette = false;
				}
			}
		});
		Time.setRepeats(false);
		Time.start();
	}
	
	
	/////////////////////////////////////////////
	//////// Classes ///////////////
	////////////////////////////////////////////

	/*public class Roulette extends Thread
	{
		String User;
		Channel Channel;
		String Event;
		Random Rand = new Random();

		public Roulette(String Name, Channel Channel, HashMap<String, Integer> Chambers, String Event)
		{
			User = Name;
			this.Channel = Channel;
			this.Event = Event;
			if (!Chambers.containsKey(Channel))
				Chambers.put(Channel.getName(), 8);
		}

		public void run()
		{
			int Result = Rand.nextInt(Chambers.get(Channel.getName()));
			Channel.send().message(User + " pulls the trigger...");
			try {
				for (int i = 0; i < 4; i++)
					Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (Result == 0) 
			{
				Channel.send().message("BANG!");
				Chambers.put(Channel.getName(), 8);
				if (isBroadcaster(User.toLowerCase(), Channel))
				{
					Channel.send().message("However, the bullet was stopped by " + User + "'s Shield of Divinity +2!");
					return;
				}
				if (isMod(Event, User))
					Channel.send().message("However, the bullet was stopped by " + User + "'s Sword of Moderation +1!");
				else
					Channel.send().message("/timeout " + User + " 180");
			} else {
				Channel.send().message("Click!");
				System.out.println("Chambers:  " + Chambers.get(Channel.getName()));
				System.out.println("Rolled:  " + Result);
				Chambers.put(Channel.getName(), Chambers.get(Channel.getName()) - 1);
			}
		}

		public void main(String args[])
		{
			(new Thread(new Roulette(User, Channel, Chambers, Event))).start();
		}
	}*/

	public class Stream
	{
		private StreamNode chatters;
	}

	public class StreamNode
	{
		private String[] moderators;
		private String[] staff;
		private String[] admins;
		private String[] global_mods;
		private String[] viewers;
	}

	public class UserNode
	{
		private String User;
	}

	static class Page
	{
		String display_name;
		String game;
		String url;
		String status; // Stream title
	}

	static class Strawpoll
	{
		String title;
		String[] options;

		public Strawpoll(String Title, String[] Options)
		{
			options = new String[Options.length];
			System.arraycopy(Options, 0, options, 0, Options.length);
			title = Title;
		}
	}

	static class StrawID
	{
		String id;
	}

	static class StrawResults
	{
		String title;
		String[] options;
		int[] votes;

		public StrawResults()
		{
			options = new String[50];
			votes = new int[50];
		}
	}

	public class UserException extends Exception
	{
		public UserException(String Msg)
		{
			super(Msg);
		}
	}

	/////////////////////////////////////////////
	//////// Song Requests ///////////////
	////////////////////////////////////////////

	/*
	 * public class PlaylistUpdater { private YouTube YT; private String
	 * VIDEO_ID;
	 * 
	 * public PlaylistUpdater() {
	 * 
	 * }
	 * 
	 * public void main(String[] args) { List<String> Scopes =
	 * Lists.newArrayList("https://www.googleapis.com/auth/youtube");
	 * 
	 * GoogleCredential try { new YouTube.Builder(new NetHttpTransport(), new
	 * JacksonFactory(), new HttpRequestInitializer() } } private String
	 * insertPlaylist() throws IOException {
	 * 
	 * // This code constructs the playlist resource that is being inserted. //
	 * It defines the playlist's title, description, and privacy status.
	 * PlaylistSnippet playlistSnippet = new PlaylistSnippet();
	 * playlistSnippet.setTitle("YateBot Playlist");
	 * playlistSnippet.setDescription(
	 * "A private playlist created with the YouTube API v3"); PlaylistStatus
	 * playlistStatus = new PlaylistStatus();
	 * playlistStatus.setPrivacyStatus("private");
	 * 
	 * Playlist youTubePlaylist = new Playlist();
	 * youTubePlaylist.setSnippet(playlistSnippet);
	 * youTubePlaylist.setStatus(playlistStatus);
	 * 
	 * // Call the API to insert the new playlist. In the API call, the first //
	 * argument identifies the resource parts that the API response should //
	 * contain, and the second argument is the playlist being inserted.
	 * YouTube.Playlists.Insert playlistInsertCommand =
	 * YT.playlists().insert("snippet,status", youTubePlaylist); Playlist
	 * playlistInserted = playlistInsertCommand.execute();
	 * 
	 * // Print data from the API response and return the new playlist's //
	 * unique playlist ID.
	 * 
	 * return playlistInserted.getId();
	 * 
	 * } }
	 */
	}
