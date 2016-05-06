/*
 * This file is part of MyDMAM.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2016
 * 
*/
package hd3gtv.mydmam.auth;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.netflix.astyanax.model.ColumnFamily;

public class AuthTurret {
	
	// TODO get an User from login + Password (+ domain)
	// TODO create new User
	
	// TODO import conf
	// TODO domain isolation or not
	
	private static final String ADMIN_USER_NAME = "admin";
	private static final String ADMIN_ROLE_NAME = "administrator";
	private static final String GUEST_USER_NAME = "guest";
	private static final String GROUP_ADMIN_NAME = "administrators";
	private static final String GROUP_NEWUSERS_NAME = "new_users";
	
	public AuthTurret() {
		/**
		 * Peuplate DB ACLs:
		 * TODO import & destroy XML account_export file:
		 * new File(Configuration.getGlobalConfigurationDirectory().getParent() + File.separator + "account_export.xml")
		 * TODO create admin role if needed, grant all privilege if it't not the actual case
		 * TODO create guest role if needed
		 * TODO create admin group if needed
		 * TODO create newusers group if needed
		 * TODO create admin user if needed, and create a password file:
		 */
		/*
		String newpassword = Password.passwordGenerator();
		authenticatorlocalsqlite.createUser(ACLUser.ADMIN_NAME, newpassword, "Local Admin", true);
		
		File textfile = new File("play-new-password.txt");
		FileWriter fw = new FileWriter(textfile, false);
		fw.write("Admin login: " + ACLUser.ADMIN_NAME + "\r\n");
		fw.write("Admin password: " + newpassword + "\r\n");
		fw.write("\r\n");
		fw.write("You should remove this file after keeping this password..\r\n");
		fw.write("\r\n");
		fw.write("You can change this password with mydmam-cli:\r\n");
		fw.write("$ mydmam-cli localauth -f " + authenticatorlocalsqlite.getDbfile().getAbsolutePath() + " -key " + authenticatorlocalsqlite.getMaster_password_key() + " -passwd -u "
				+ ACLUser.ADMIN_NAME + "\r\n");
		fw.write("\r\n");
		fw.write("Note: you haven't need a local authenticator if you set another backend and if you grant some new administrators\r\n");
		fw.close();
		
		Loggers.Auth.info(
				"Create Play administrator account, login: " + ACLUser.ADMIN_NAME + ", password file: " + textfile.getAbsoluteFile() + ", local database: " + authenticatorlocalsqlite.getDbfile());
				
		if (authenticatorlocalsqlite.isEnabledUser(ACLUser.ADMIN_NAME) == false) {
			throw new Exception("User " + ACLUser.ADMIN_NAME + " is disabled in sqlite file !");
		}
		 * */
	}
	
	public UserNG authenticate(String remote_address, String username, String password, String domain, String language) throws InvalidUserAuthentificationException {
		if (remote_address == null) {
			throw new NullPointerException("\"remote_address\" can't to be null");
		}
		if (username == null) {
			throw new NullPointerException("\"username\" can't to be null");
		}
		if (password == null) {
			throw new NullPointerException("\"password\" can't to be null");
		}
		if (domain == null) {
			return authenticate(remote_address, username, password, language);
		}
		if (domain.trim().isEmpty()) {
			return authenticate(remote_address, username, password, language);
		}
		// TODO authenticate for domain
		/*try {
			authenticationUser = authenticator.getUser(username, password);
			if (authenticationUser != null) {
				Loggers.Auth.debug("Valid user found for this authentication method, username: " + username + ", " + authenticator);
				return authenticationUser;
			}
		} catch (IOException e) {
			Loggers.Auth.error("Invalid authentication method: " + username + ", " + authenticator, e);
		}*/
		
		// TODO if user don't exists in db, add it (w/o password) in new_user group
		// TODO sync user long name, email, groups, and last-edit if user is from AD
		// TODO set user.doLoginOperations(remote_address, language) + save
		
		return null;
	}
	
	public UserNG authenticate(String remote_address, String username, String password, String language) throws InvalidUserAuthentificationException {
		// TODO authenticate for each domain
		/*for (int pos = 0; pos < authenticators.size(); pos++) {
			try {
				
				authenticationUser = authenticate(authenticators.get(pos), username, password);
				if (authenticationUser != null) {
					return authenticationUser;
				}
			} catch (InvalidAuthenticatorUserException e) {
				Loggers.Auth.debug("Invalid user for this authentication method, authenticator: " + authenticators.get(pos), e);
			}
		}
		throw new InvalidAuthenticatorUserException("Can't authenticate with " + username);*/
		return null;
	}
	
	private Gson gson_simple;// TODO set
	private Gson gson;// TODO set
	private ColumnFamily<String, String> user_cf;// TODO set
	
	public Gson getGson() {
		return gson;
	}
	
	public Gson getGsonSimple() {
		return gson_simple;
	}
	
	public boolean isForceSelectDomain() {
		// TODO from conf
		return false;
	}
	
	public ArrayList<String> declaredDomainList() {
		// TODO from conf
		return new ArrayList<String>();
	}
	
	public UserNG getByUserKey(String user_key) {
		// TODO
		return null;
	}
	
}
