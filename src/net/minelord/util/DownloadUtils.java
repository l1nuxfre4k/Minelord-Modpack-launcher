/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.minelord.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Scanner;

import net.minelord.data.Settings;
import net.minelord.gui.LaunchFrame;
import net.minelord.gui.dialogs.AdvancedOptionsDialog;
import net.minelord.log.Logger;

public class DownloadUtils extends Thread
{
	public static boolean serversLoaded = false;
	public static HashMap<String, String> downloadServers = new HashMap<String, String>();
	private static String currentmd5 = "";

	/**
	 * @param file
	 *            - the name of the file, as saved to the repo (including
	 *            extension)
	 * @return - the direct link
	 * @throws NoSuchAlgorithmException
	 *             - see md5
	 */
	public static String getMinelordLink(String file) throws NoSuchAlgorithmException
	{
		if (currentmd5.isEmpty())
		{
			currentmd5 = md5("mcepoch1" + getTime());
		}
		String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : "http://www.creeperrepo.net";
		resolved += "/direct/FTB2/" + currentmd5 + "/" + file;
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) new URL(resolved).openConnection();
			for (String server : downloadServers.values())
			{
				if (connection.getResponseCode() != 200 && !server.equalsIgnoreCase("www.minelord.com"))
				{
					resolved = "http://" + server + "/direct/FTB2/" + currentmd5 + "/" + file;
					connection = (HttpURLConnection) new URL(resolved).openConnection();
				}
			}
		}
		catch (IOException e)
		{
		}
		connection.disconnect();
		Logger.logInfo(resolved);
		return resolved;
	}

	/**
	 * @param file
	 *            - the name of the file, as saved to the repo (including
	 *            extension)
	 * @return - the direct link
	 */
	public static String getStaticMinelordLink(String file)
	{
		return "http://launcher.minelord.com/static/modpack/"+file;
	}

	/**
	 * @param file
	 *            - file on the repo in static
	 * @return boolean representing if the file exists
	 */
	public static boolean staticFileExists(String file)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getStaticMinelordLink(file)).openStream()));
			return !reader.readLine().toLowerCase().contains("not found");
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * @param file
	 *            - file on the repo
	 * @return boolean representing if the file exists
	 */
	public static boolean fileExists(String file)
	{
		try
		{
			if (currentmd5.isEmpty())
			{
				currentmd5 = md5("mcepoch1" + getTime());
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://www.creeperrepo.net/direct/FTB2/" + currentmd5 + "/" + file).openStream()));
			return !reader.readLine().toLowerCase().contains("not found");
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * @param input
	 *            - String to hash
	 * @return - hashed string
	 * @throws NoSuchAlgorithmException
	 *             - in case "MD5" isnt a correct input
	 */
	public static String md5(String input) throws NoSuchAlgorithmException
	{
		String result = input;
		if (input != null)
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32)
			{
				result = "0" + result;
			}
		}
		return result;
	}

	/**
	 * gets the time from the creeperhost servers
	 * 
	 * @return - the time in the DDMMYY format
	 */
	public static String getTime()
	{
		String content = null;
		Scanner scanner = null;
		String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : "http://www.creeperrepo.net";
		resolved += "/getdate";
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) new URL(resolved).openConnection();
			if (connection.getResponseCode() != 200)
			{
				for (String server : downloadServers.values())
				{
					if (connection.getResponseCode() != 200 && !server.equalsIgnoreCase("www.creeperrepo.net"))
					{
						resolved = "http://" + server + "/getdate";
						connection = (HttpURLConnection) new URL(resolved).openConnection();
					}
					else
						if (connection.getResponseCode() == 200)
						{
							break;
						}
				}
			}
			scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			content = scanner.next();
		}
		catch (IOException e)
		{
		}
		finally
		{
			connection.disconnect();
			if (scanner != null)
			{
				scanner.close();
			}
		}
		return content;
	}

	/**
	 * Downloads data from the given URL and saves it to the given file
	 * 
	 * @param filename
	 *            - String of destination
	 * @param urlString
	 *            - http location of file to download
	 */
	public static void downloadToFile(String filename, String urlString) throws IOException
	{
		downloadToFile(new URL(urlString), new File(filename));
	}

	/**
	 * Downloads data from the given URL and saves it to the given file
	 * 
	 * @param url
	 *            The url to download from
	 * @param file
	 *            The file to save to.
	 */
	public static void downloadToFile(URL url, File file) throws IOException
	{
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(file);
		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		fos.close();
	}

	/**
	 * Checks the file for corruption.
	 * 
	 * @param file
	 *            - File to check
	 * @return boolean representing if it is valid
	 * @throws IOException
	 */
	public static boolean isValid(File file, String url) throws IOException
	{
		String content = null;
		Scanner scanner = null;
		String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : "http://www.creeperrepo.net";
		resolved += "/md5/FTB2/" + url;
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) new URL(resolved).openConnection();
			if (connection.getResponseCode() != 200)
			{
				for (String server : downloadServers.values())
				{
					if (connection.getResponseCode() != 200 && !server.equalsIgnoreCase("www.creeperrepo.net"))
					{
						resolved = "http://" + server + "/md5/FTB2/" + url;
						connection = (HttpURLConnection) new URL(resolved).openConnection();
					}
					else
						if (connection.getResponseCode() == 200)
						{
							break;
						}
				}
			}
			scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			content = scanner.next();
		}
		catch (IOException e)
		{
		}
		finally
		{
			connection.disconnect();
			if (scanner != null)
			{
				scanner.close();
			}
		}
		String result = fileMD5(file);
		Logger.logInfo("Local: " + result.toUpperCase());
		Logger.logInfo("Remote: " + content.toUpperCase());
		return content.equalsIgnoreCase(result);
	}

	/**
	 * Gets the md5 of the downloaded file
	 * 
	 * @param file
	 *            - File to check
	 * @return - string of file's md5
	 * @throws IOException
	 */
	private static String fileMD5(File file) throws IOException
	{
		if (!file.exists())
		{
			return "";
		}
		URL fileUrl = file.toURI().toURL();
		MessageDigest dgest = null;
		try
		{
			dgest = MessageDigest.getInstance("md5");
		}
		catch (NoSuchAlgorithmException e)
		{
		}
		InputStream str = fileUrl.openStream();
		byte[] buffer = new byte[65536];
		int readLen;
		while ((readLen = str.read(buffer, 0, buffer.length)) != -1)
		{
			dgest.update(buffer, 0, readLen);
		}
		str.close();
		Formatter fmt = new Formatter();
		for (byte b : dgest.digest())
		{
			fmt.format("%02X", b);
		}
		return fmt.toString();
	}

	/**
	 * Used to load all available download servers in a thread to prevent wait.
	 */
	@Override
	public void run()
	{
		downloadServers.put("Minelord", "launcher.minelord.com");
		serversLoaded = true;
		if (LaunchFrame.getInstance() != null && LaunchFrame.getInstance().optionsPane != null)
		{
			AdvancedOptionsDialog.setDownloadServers();
		}
	}
}
