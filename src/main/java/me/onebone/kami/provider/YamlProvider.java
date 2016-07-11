package me.onebone.kami.provider;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import me.onebone.kami.Group;
import me.onebone.kami.Kami;

public class YamlProvider implements Provider{
	private File baseFolder;
	
	public YamlProvider(Kami plugin){
		this.baseFolder = new File(plugin.getDataFolder(), "players");
		
		if(!this.baseFolder.exists()){
			this.baseFolder.mkdir();
		}
	}
	
	public boolean playerExists(String player){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player);
		return file.isFile();
	}
	
	@SuppressWarnings("serial")
	public void addPlayer(String player, final String group){
		Config config = new Config(new File(this.baseFolder, player.toLowerCase() + ".yml"), Config.YAML);
		config.setAll(new ConfigSection(){
			{
				set("name", player);
				set("group", group);
				set("permission", new ArrayList<String>());
			}
		});
		config.save();
	}

	public Map<String, Object> getPlayer(String player){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		if(file.isFile()){
			return new Config(file, Config.YAML).getAll();
		}
		return null;
	}

	public void setGroup(String player, String group){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		Config config = new Config(file, Config.YAML);
		config.set("group", group);
		config.save();
	}
	
	@Override
	public boolean addPermission(String player, String permission){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		Config config = new Config(file, Config.YAML);
		
		List<String> perms = config.get("permissions", new ArrayList<String>());
		if(perms.contains(permission)){
			return false;
		}
		perms.add(permission);
		config.set("permissions", perms);
		config.save();
		return true;
	}
	
	@Override
	public boolean removePermission(String player, String permission){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		if(file.exists()){
			Config config = new Config(file, Config.YAML);
			
			List<String> perms = config.get("permissions", new ArrayList<String>());
			perms.remove(permission);
			config.set("permissions", perms);
			config.save();
			return true;
		}
		return false;
	}

	public void removePlayer(String player){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		if(file.exists()){
			file.delete();
		}
	}
	
	public List<String> getPermissions(String player){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		if(file.isFile()){
			return new Config(file, Config.YAML).get("permissions", new ArrayList<String>());
		}
		return new ArrayList<String>();
	}

	@Override
	public String getGroup(String player){
		player = player.toLowerCase();
		
		File file = new File(this.baseFolder, player + ".yml");
		if(file.isFile()){
			return new Config(file, Config.YAML).get("group", Group.getDefaultGroup().getName());
		}
		return Group.getDefaultGroup().getName();
	}
}
