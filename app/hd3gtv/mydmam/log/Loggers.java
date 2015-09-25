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
 * Copyright (C) hdsdi3g for hd3g.tv 2015
 * 
*/
package hd3gtv.mydmam.log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import hd3gtv.log2.Log2Dump;

public enum Loggers {
	
	LogTest;
	
	private Logger current;
	
	private Loggers() {
		current = Logger.getLogger(this.name());
	}
	
	public Logger logger() {
		return current;
	}
	
	public void assertLog(boolean assertion, Level level, String message) {
		if (assertion == false) {
			current.log(level, message);
		}
	}
	
	public void trace(String message) {
		current.log(Level.TRACE, message);
	}
	
	public void debug(String message) {
		current.log(Level.DEBUG, message);
	}
	
	public void info(String message) {
		current.log(Level.INFO, message);
	}
	
	public void warn(String message) {
		current.log(Level.WARN, message);
	}
	
	public void warn(String message, Exception e) {
		current.log(Level.WARN, message, e);
	}
	
	public void error(String message, Exception e) {
		current.log(Level.ERROR, message, e);
	}
	
	public void fatal(String message, Exception e) {
		current.log(Level.FATAL, message, e);
	}
	
	/**
	 * @param e maybe null
	 */
	public void logAndDump(String message, Level level_message, Exception e, Log2Dump dump, Level level_dump) {
		current.log(level_message, message, e);
		
		if (current.isEnabledFor(level_dump)) {
			ArrayList<String> all_dumps = dump.dumptoString();
			for (int pos = 0; pos < all_dumps.size(); pos++) {
				current.log(level_dump, all_dumps.get(pos), e);
			}
		}
	}
	
	/**
	 * @param message display at Info
	 * @param dump display at Debug, never null
	 */
	public void infoDump(String message, Log2Dump dump) {
		logAndDump(message, Level.INFO, null, dump, Level.DEBUG);
	}
	
	/**
	 * @param message display at Error
	 * @param e display at Error, maybe null
	 * @param dump display at Debug, never null
	 */
	public void errorDump(String message, Exception e, Log2Dump dump) {
		logAndDump(message, Level.ERROR, e, dump, Level.DEBUG);
	}
	
	/**
	 * For all declared Loggers, even outside this code.
	 */
	public static Map<String, Level> getAllLevels() {
		HashMap<String, Level> result = new HashMap<String, Level>();
		
		@SuppressWarnings("unchecked")
		Enumeration<Logger> all_loggers = LogManager.getCurrentLoggers();
		
		Logger item;
		while (all_loggers.hasMoreElements()) {
			item = all_loggers.nextElement();
			result.put(item.getName(), item.getEffectiveLevel());
		}
		
		return result;
	}
	
	/**
	 * For all declared Loggers, even outside this code.
	 */
	public static void changeLevel(String logger_name, Level new_level) {
		Logger l = Logger.getLogger(logger_name);
		l.setLevel(new_level);
	}
	
	public static void changeRootLevel(Level new_level) {
		Logger.getRootLogger().setLevel(new_level);
	}
}
