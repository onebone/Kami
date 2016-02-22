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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.onebone.kami.event.group.GroupPermissionChangedEvent;
import me.onebone.kami.provider.Provider;
import me.onebone.kami.provider.YamlProvider;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInvalidMoveEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class Kami extends PluginBase implements Listener{
	private Provider provider;
	private Config groups;
	
	private HashMap<String, User> users;
	
	@SuppressWarnings("unchecked")
	public void onEnable(){
		this.saveDefaultConfig();
		this.saveResource("groups.yml");
		
		this.provider = new YamlProvider(this);
		
		groups = new Config(new File(this.getDataFolder(), "groups.yml"), Config.YAML);
		groups.getAll().forEach((k, v) -> {
			try{
				new Group(this, k, (LinkedHashMap<String, Object>)v);
			}catch(Exception e){
				this.getLogger().warning("An exception while parsing group " + k + " was found.");
				this.getLogger().error(e.getMessage());
			}
		});
		
		users = new HashMap<String, User>();
		
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onPermissionChanged(GroupPermissionChangedEvent event){
		if(this.getConfig().getBoolean("reapply-on-change", true)){
			this.users.forEach((k, v) -> {
				v.apply();
			});
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if(!this.provider.playerExists(player.getName())){
			Group defaultGroup = Group.getDefaultGroup();
			if(defaultGroup == null){
				this.getLogger().critical("You don't have default group.");
				return;
			}
			
			User user = new User(this, player, this.provider);
			this.users.put(player.getName().toLowerCase(), user);
			
			this.provider.addPlayer(player.getName(), defaultGroup.getName());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		
		if(users.containsKey(player.getName().toLowerCase())){
			users.remove(player.getName().toLowerCase());
		}
	}
	
	@EventHandler
	public void onInvalidMove(PlayerInvalidMoveEvent event){
		if(event.getPlayer().hasPermission("kami.invalidmove")){
			event.setCancelled();
		}
	}
	
	@SuppressWarnings("serial")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(command.getName().equals("addgroup")){
			Map<String, String> argMap;
			try{
				argMap = getArguments("d", args);
			}catch(Exception e){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			if(argMap == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(!argMap.containsKey("")){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}
			String group = argMap.get("");
			if(Group.getGroup(group) != null){
				sender.sendMessage(TextFormat.RED + "Group " + group + " already exists.");
				return true;
			}
			
			new Group(this, group, new LinkedHashMap<String, Object>(){
				{
					put("default", argMap.containsKey("d"));
				}
			});
			
			this.groups.set(group, new LinkedHashMap<String, Object>(){
				{
					put("default", argMap.containsKey("d"));
					put("permissions", new ArrayList<String>());
					put("extends", new ArrayList<String>());
				}
			});
			this.groups.save();
			
			sender.sendMessage(TextFormat.GREEN + "Group " + group + " was added successfully.");
			return true;
		}else if(command.getName().equals("rmgroup")){
			Map<String, String> argMap;
			try{
				argMap = getArguments("", args);
			}catch(Exception e){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			if(argMap == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(!argMap.containsKey("")){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}

			String name = argMap.get("");
			Group group = Group.getGroup(name);
			if(group == null){
				sender.sendMessage(TextFormat.RED + "Please provide existing group name.");
				return true;
			}
			
			Group defaultGroup = Group.getDefaultGroup();
			this.users.forEach((k, v) -> {
				v.setGroup(defaultGroup);
				v.getPlayer().sendMessage("You have been changed your group to " + defaultGroup.getName());
			});
			sender.sendMessage(TextFormat.GREEN + "Removed group " + group.getName());
			return true;
		}else if(command.getName().equals("usermod")){
			Map<String, String> argMap;
			try{
				argMap = getArguments("g:a:r:", args);
			}catch(Exception e){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(argMap == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(!argMap.containsKey("")){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}
			String player = argMap.get("");
			
			int action = 0, errors = 0;
			
			loop:
			for(String key : argMap.keySet()){
				if(key.equals("")) continue;
				
				switch(key){
				case "g":
					String name = argMap.get("g");
					Group group = Group.getGroup(name);
					if(group != null){
						Player p;
						if((p = this.getServer().getPlayer(player)) instanceof Player){
							player = p.getName();
							
							if(users.containsKey(player.toLowerCase())){
								users.get(player.toLowerCase()).setGroup(group);
							}
							p.sendMessage("You have been changed your group to " + group.getName());
						}
						
						if(this.users.containsKey(player.toLowerCase())){
							this.users.get(player.toLowerCase()).setGroup(group);
						}else{
							this.provider.setGroup(player, name);
						}
					}else{
						errors++;
						sender.sendMessage(TextFormat.RED + "Invalid group name was given.");
					}
					action++;
					continue loop;
				case "a":
					action++;
					
					String append = argMap.get("a");
					
					Player p;
					if((p = this.getServer().getPlayer(player)) instanceof Player){
						player = p.getName();
					}
					if(this.users.containsKey(player.toLowerCase())){
						if(this.users.get(player.toLowerCase()).addPermission(append)){
							sender.sendMessage(TextFormat.GREEN + "Added " + append + " to " + player);
						}else{
							sender.sendMessage(TextFormat.RED + player + " does not have permission " + append);
							errors++;
						}
					}else{
						if(this.provider.addPermission(player, append)){
							sender.sendMessage(TextFormat.GREEN + "Added " + append + " to " + player);
						}else{
							sender.sendMessage(TextFormat.RED + player + " does not have permission " + append);
							errors++;
						}
					}
					continue loop;
				case "r":
					action++;
					
					String remove = argMap.get("r");
					Player p1;
					if((p1 = this.getServer().getPlayer(player)) instanceof Player){
						player = p1.getName();
					}
					if(this.users.containsKey(player.toLowerCase())){
						if(this.users.get(player.toLowerCase()).removePermission(remove)){
							sender.sendMessage(TextFormat.GREEN + "Removed " + remove + " from " + player);
						}else{
							sender.sendMessage(TextFormat.RED + player + " does not have permission " + remove);
							errors++;
						}
					}else{
						if(this.provider.removePermission(player, remove)){
							sender.sendMessage(TextFormat.GREEN + "Removed " + remove + " from " + player);
						}else{
							sender.sendMessage(TextFormat.RED + player + " does not have permission " + remove);
							errors++;
						}
					}
					continue loop;
				}
			}
			
			if(action <= 0){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
			}else if(errors <= 0){
				sender.sendMessage(TextFormat.GREEN + "All actions were successful.");
			}else{
				sender.sendMessage(TextFormat.YELLOW + "Some actions were not successful.");
			}
			return true;
		}else if(command.getName().equals("groups")){
			StringBuilder builder = new StringBuilder();
			builder.append("Groups: ");
			
			Group.getGroups().forEach((k, group) -> {
				builder.append(group.getName() + ", ");
			});

			sender.sendMessage(builder.substring(0, builder.length() - 2));
			return true;
		}else if(command.getName().equals("group")){
			Map<String, String> argMap;
			try{
				argMap = getArguments("da:r:", args);
			}catch(Exception e){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(argMap == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(!argMap.containsKey("")){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}
			
			Group group = Group.getGroup(argMap.get(""));
			if(group == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid group.");
				return true;
			}
			
			int actions = 0, errors = 0;
			
			loop:
			for(String key : argMap.keySet()){
				if(key.equals("")) continue;
				
				switch(key){
				case "d":
					group.setDefault();
					// TODO: save
					actions++;
					continue loop;
				case "a":
					actions++;
					
					String append = argMap.get("a");
					if(group.addPermission(append)){
						this.groups.save();
					}else{
						sender.sendMessage(TextFormat.RED + append + " already exists!");
						errors++;
					}
					continue loop;
				case "r":
					actions++;
					
					String remove = argMap.get("r");
					if(group.removePermission(remove)){
						this.groups.save();
					}else{
						sender.sendMessage(TextFormat.RED + remove + " does not exist in group " + group.getName());
						errors++;
					}
					continue loop;
				}
			}
			if(actions <= 0){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
			}else if(errors <= 0){
				sender.sendMessage(TextFormat.GREEN + "All actions were successful.");
			}else{
				sender.sendMessage(TextFormat.YELLOW + "Some actions were not successful.");
			}
			return true;
		}else if(command.getName().equals("user")){
			Map<String, String> argMap;
			try{
				argMap = getArguments("", args);
			}catch(Exception e){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(argMap == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			if(!argMap.containsKey("")){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}
			
			String player = argMap.get("");
			Player p;
			if((p = this.getServer().getPlayer(player)) instanceof Player){
				player = p.getName();
			}
			
			Map<String, Object> user = this.provider.getPlayer(player);
			if(user == null){
				sender.sendMessage(TextFormat.RED + "There is no data for " + player);
			}else{
				sender.sendMessage(TextFormat.GREEN + "User data for: " + player + "\n"
									+ "Group: " + user.getOrDefault("group", Group.getDefaultGroup().getName()) + "\n");
			}
		}else if(command.getName().equals("perms")){
			Map<String, String> argMap;
			try{
				argMap = getArguments("u:g:p:", args);
			}catch(Exception e){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(argMap == null){
				sender.sendMessage(TextFormat.RED + "Please provide valid parameters.");
				return true;
			}
			
			if(!argMap.containsKey("p")){
				argMap.put("p", "1");
			}
			
			if(argMap.containsKey("u")){
				try{
					String player = argMap.get("u");
					int page = Integer.parseInt(argMap.get("p"));
					List<String> perms = this.provider.getPermissions(player.toLowerCase());
					
					page = (int)Math.max(1, Math.min(page, Math.ceil((double)perms.size() / 5.0)));
					
					StringBuilder builder = new StringBuilder("List of permissions of user: " + player + ": (" + page + " / " + (int)Math.ceil((double)perms.size() / 5.0) + ")\n");
					for(int i = 0; i < perms.size(); i++){
						int current = (int)Math.ceil((double)(i + 1) / (double)perms.size());
						
						if(current == page){
							builder.append(perms.get(i) + "\n");
						}else if(current > page){
							break;
						}
					}
					sender.sendMessage(builder.substring(0, builder.length() - 1));
				}catch(NumberFormatException e){
					sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				}
				return true;
			}
			if(argMap.containsKey("g")){
				try{
					Group group = Group.getGroup(argMap.get("g"));
					if(group != null){
						int page = Integer.parseInt(argMap.get("p"));
						List<String> perms = group.getPermissionsRaw();
						
						page = (int)Math.max(1, Math.min(page, Math.ceil((double)perms.size() / 5.0)));
						
						StringBuilder builder = new StringBuilder("List of permissions of group: " + group.getName() + ": (" + page + " / " + (int)Math.ceil((double)perms.size() / 5.0) + ")\n");
						for(int i = 0; i < perms.size(); i++){
							int current = (int)Math.ceil((double)(i + 1) / (double)perms.size());
							
							if(current == page){
								builder.append(perms.get(i) + "\n");
							}else if(current > page){
								break;
							}
						}
						sender.sendMessage(builder.substring(0, builder.length() - 1));
					}else{
						sender.sendMessage(TextFormat.RED + "Please provide existing group.");
					}
				}catch(NumberFormatException e){
					sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				}
				return true;
			}
			sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
			return true;
		}
		return false;
	}
	
	public static Map<String, String> getArguments(String options, String[] args){
		Map<String, String> ret = new HashMap<String, String>();
		Map<String, Boolean> option = new HashMap<String, Boolean>();
		String[] optionArr = options.split("");

		for(int i = 0; i < optionArr.length; i++){
			String flag = optionArr[i];
			
			if(optionArr.length - 1 > i && optionArr[i + 1].equals(":")){
				option.put(flag, true);
				i++;
			}else{
				option.put(flag, false);
			}
		}
		option.put("", true);
		
		for(int i = 0; i < args.length; i++){
			String flag = args[i];
			
			if(flag.startsWith("-")){
				if(args.length <= i + 1){
					ret.put(flag.substring(1), "");
					break;
				}
				flag = flag.substring(1);
				if(option.containsKey(flag) && option.get(flag) != false){
					if(args.length <= i + 1){
						ret.put(flag, "");
						break;
					}
					String data = args[++i];
					if(data.startsWith("\"")){
						try{
							String temp;
							do{
								temp = args[++i];
								data += (" " + temp);
							}while(!temp.endsWith("\""));

							data = data.substring(1, data.length() - 1);
						}catch(ArrayIndexOutOfBoundsException e){
							return null;
						}
					}
					ret.put(flag, data);
				}else{
					ret.put(flag, null);
				}
			}else{
				if(args.length <= i + 1){
					ret.put("", flag);
					break;
				}
				if(flag.startsWith("\"")){
					try{
						String temp;
						do{
							temp = args[++i];
							flag += (" " + temp);
						}while(!temp.endsWith("\""));

						flag = flag.substring(1, flag.length() - 1);
					}catch(ArrayIndexOutOfBoundsException e){
						return null;
					}
				}
				ret.put("", flag);
			}
		}
		
		return ret;
	}
	
	public void onDisable(){
		users.clear();
	}
}
