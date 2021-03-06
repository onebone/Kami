package me.onebone.kami.event.group;

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

import me.onebone.kami.Group;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class GroupPermissionChangedEvent extends Event{
	public static HandlerList handlers = new HandlerList();
	
	public static final int TYPE_ADD = 0;
	public static final int TYPE_REMOVE = 1;
	
	private int type;
	private String permission;
	private Group group;
	
	public GroupPermissionChangedEvent(Group group, String permission, int type){
		this.group = group;
		this.permission = permission;
		this.type = type;
	}
	
	public Group getGroup(){
		return this.group;
	}
	
	public String getPermission(){
		return this.permission;
	}
	
	public int getType(){
		return this.type;
	}
	
	public static HandlerList getHandlers(){
		return handlers;
	}
}
