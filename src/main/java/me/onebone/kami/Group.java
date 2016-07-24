package me.onebone.kami;

/*
 * Kami: A typical permission management plugin for Nukkit
 * Copyright (C) 2016 onebone <jyc00410@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import me.onebone.kami.event.group.GroupPermissionChangedEvent;

public class Group{
	private Kami plugin;
	private static Group defaultGroup = null;

	private static HashMap<String, Group> groups = new HashMap<>();
	private static int defaults = 0;
	
	private boolean isDefault = false;
	private List<String> permissions;
	private List<String> parents;
	private String name;
	
	public static Group getGroup(String name){
		return groups.get(name.toLowerCase());
	}
	
	public static Group getDefaultGroup(){
		return defaultGroup;
	}
	
	public static HashMap<String, Group> getGroups(){
		return new HashMap<String, Group>(groups);
	}
	
	@SuppressWarnings("unchecked")
	public Group(Kami plugin, String name, LinkedHashMap<String, Object> data){
		this.plugin = plugin;
		
		this.name = name;
		this.isDefault = (boolean) data.getOrDefault("default", false);

		this.permissions = ((List<String>)data.getOrDefault("permissions", new ArrayList<String>()));
		
		this.parents = (List<String>)data.getOrDefault("extends", null);
		
		groups.put(name.toLowerCase(), this);
		if(this.isDefault){
			defaultGroup = this;
			
			if(++defaults > 1){
				plugin.getLogger().warning("You have more than one default group. Last group which have been specified default will be applied.");
			}
		}
	}
	
	private void applyParents(Map<String, Boolean> tree){
		if(this.parents == null) return;
		
		this.parents.forEach((parent) -> {
			if(groups.containsKey(parent.toLowerCase())){
				Map<String, Boolean> permissions = groups.get(parent.toLowerCase()).getPermissions();
				permissions.forEach((k, v) -> {
					if(!tree.containsKey(k)){
						tree.put(k, v);
					}
				});
			}
		});
	}
	
	public Map<String, Boolean> getPermissions(){
		SortedMap<String, Boolean> permissions = new TreeMap<String, Boolean>(new PermissionComparator());
		this.applyParents(permissions);
		
		this.permissions.forEach((v) -> {
			if(v.startsWith("-")){
				for(String node : User.parseWildcard(v.substring(1), plugin.getServer().getPluginManager().getPermissions().keySet())){
					permissions.put(node, false);
				}
			}else{
				for(String node : User.parseWildcard(v, plugin.getServer().getPluginManager().getPermissions().keySet())){
					permissions.put(node, true);
				}
			}
		});
		return permissions;
	}
	
	public List<String> getPermissionsRaw(){
		return this.permissions;
	}
	
	public boolean addPermission(String permission){
		if(!this.permissions.contains(permission)){
			this.permissions.add(permission);
			
			this.plugin.getServer().getPluginManager().callEvent(new GroupPermissionChangedEvent(this, permission, GroupPermissionChangedEvent.TYPE_ADD));
			return true;
		}
		return false;
	}
	
	public boolean removePermission(String permission){
		if(this.permissions.remove(permission)){
			this.plugin.getServer().getPluginManager().callEvent(new GroupPermissionChangedEvent(this, permission, GroupPermissionChangedEvent.TYPE_REMOVE));
			return true;
		}
		return false;
	}
	
	public void setDefault(){
		if(defaultGroup != null){
			defaultGroup = null;
			defaults--;
		}
		
		defaultGroup = this;
		defaults++;
	}
	
	public boolean isDefault(){
		return isDefault;
	}
	
	public String getName(){
		return this.name;
	}
}
