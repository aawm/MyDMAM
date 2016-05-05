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
 * Copyright (C) hdsdi3g for hd3g.tv 2014
 * 
*/
package models;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.auth.DbAccountExtractor;
import hd3gtv.mydmam.auth.SelfExtractor;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.JPABase;

/**
 * @deprecated
 */
@Entity
public class ACLRole extends GenericModel implements SelfExtractor {
	
	public static final String ADMIN_NAME = "administrator";
	public static final String GUEST_NAME = "guest";
	
	@Id
	@Required
	public String name;
	
	/**
	 * JSON Array
	 */
	@Lob
	public String privileges;
	
	/**
	 * JSON Array
	 */
	@Lob
	public String functionalities;
	
	@OneToMany(mappedBy = "role", cascade = CascadeType.PERSIST)
	public List<ACLGroup> groups;
	
	public ACLRole(String name) {
		this.name = name;
		this.privileges = "[]";
	}
	
	/**
	 * @return never null
	 */
	public List<String> getPrivilegesList() {
		if (privileges == null) {
			return new ArrayList<String>(1);
		}
		if (privileges.trim().equals("")) {
			return new ArrayList<String>(1);
		}
		JSONParser jp = new JSONParser();
		try {
			JSONArray ja = (JSONArray) jp.parse(privileges);
			if (ja.size() == 0) {
				return new ArrayList<String>(1);
			}
			
			ArrayList<String> result = new ArrayList<String>();
			for (Object o : ja) {
				result.add((String) o);
			}
			
			return result;
		} catch (Exception e) {
			Loggers.Play.error("Can't extract privileges from DB: " + privileges, e);
			return new ArrayList<String>(1);
		}
		
	}
	
	/**
	 * @return never null
	 */
	public List<String> getFunctionalitiesList() {
		if (functionalities == null) {
			return new ArrayList<String>(1);
		}
		if (functionalities.trim().equals("")) {
			return new ArrayList<String>(1);
		}
		JSONParser jp = new JSONParser();
		try {
			JSONArray ja = (JSONArray) jp.parse(functionalities);
			if (ja.size() == 0) {
				return new ArrayList<String>(1);
			}
			
			ArrayList<String> result = new ArrayList<String>();
			for (Object o : ja) {
				result.add((String) o);
			}
			
			return result;
		} catch (Exception e) {
			Loggers.Play.error("Can't extract functionalities from DB: " + functionalities, e);
			return new ArrayList<String>(1);
		}
	}
	
	public List<String> getFunctionalitiesBasenameList() {
		return new ArrayList<String>();
	}
	
	public <T extends JPABase> T save() {
		DbAccountExtractor.extractor.save();
		return super.save();
	}
	
	public Element exportToXML(Document document) {
		Element root = document.createElement("aclrole");
		root.setAttribute("name", name);
		
		List<String> p_list = getPrivilegesList();
		for (int pos = 0; pos < p_list.size(); pos++) {
			Element p = document.createElement("privilege");
			p.setAttribute("name", p_list.get(pos));
			root.appendChild(p);
		}
		
		return root;
	}
	
}
