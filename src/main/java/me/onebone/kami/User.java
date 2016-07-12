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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import me.onebone.kami.event.user.UserPermissionChangedEvent;
import me.onebone.kami.provider.Provider;
import cn.nukkit.Player;
import cn.nukkit.permission.PermissionAttachment;

public class User{
	private Kami plugin;
	private Group group;
	private Player player;
	private Provider provider;
	private List<String> permissions;
	private PermissionAttachment attachment;
	
	public User(Kami plugin, Player player, Provider provider){
		this.plugin = plugin;
		
		this.player = player;
		this.group = Group.getGroup(provider.getGroup(player.getName()));
		if(group == null)
			this.group = Group.getDefaultGroup();
		this.provider = provider;
		this.permissions = provider.getPermissions(player.getName());
		
		this.apply();
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public void apply(){
		if(attachment == null){
			attachment = player.addAttachment(plugin);
		}
		
		attachment.clearPermissions();
		
		SortedMap<String, Boolean> permissions = new TreeMap<String, Boolean>(new PermissionComparator());
		permissions.putAll(group.getPermissions());
		permissions.putAll(this.getPermissions());
		
		attachment.setPermissions(permissions);
	}
	
	public Map<String, Boolean> getPermissions(){
		Map<String, Boolean> permissions = new HashMap<String, Boolean>();
		
		this.permissions.forEach((v) -> {
			if(v.startsWith("-")){
				for(String node : parseWildcard(v.substring(1), plugin.getServer().getPluginManager().getPermissions().keySet())){
					permissions.put(node, false);
				}
			}else{
				for(String node : parseWildcard(v, plugin.getServer().getPluginManager().getPermissions().keySet())){
					permissions.put(node, true);
				}
			}
		});
		
		return permissions;
	}
	
	public List<String> getPermissionRaw(){
		return this.permissions;
	}
	
	public void setGroup(Group group){
		if(group == null) throw new NullPointerException("Group must not be null");
		
		this.group = group;
		this.provider.setGroup(player.getName(), group.getName());
		
		this.apply();
	}
	
	public boolean addPermission(String permission){
		if(!this.permissions.contains(permission)){
			this.permissions.add(permission);
			this.provider.addPermission(this.player.getName(), permission);
			
			this.apply();
			
			this.plugin.getServer().getPluginManager().callEvent(new UserPermissionChangedEvent(this, permission, UserPermissionChangedEvent.TYPE_ADD));
			return true;
		}
		return false;
	}
	
	public boolean removePermission(String permission){
		if(this.permissions.remove(permission)){
			this.provider.removePermission(this.player.getName(), permission);
			this.apply();
			
			this.plugin.getServer().getPluginManager().callEvent(new UserPermissionChangedEvent(this, permission, UserPermissionChangedEvent.TYPE_REMOVE));
			return true;
		}
		return false;
	}

	public static Set<String> parseWildcard(String permission, Set<String> permissions){
		Set<String> ret = new HashSet<String>();
		
		if(permission.contains("*")){
			for(String origin : permissions){
				if(matches(permission, origin)){
					ret.add(origin);
				}
			}
		}else{
			ret.add(permission);
		}
		
		return ret;
	}
	
	public static boolean matches(String perm, String raw){
		String[] perms = perm.split("\\.");
		String[] raws = raw.split("\\.");

		boolean wildcard = false;
		for(int i = 0; i < perms.length; i++){
			wildcard = perms[i].equals("*");
			if(wildcard && i == perms.length - 1) return true;

			if(raws.length <= i || (!wildcard && !perms[i].equals(raws[i]))){
				return false;
			}
		}
		
		return true;
	}
}
