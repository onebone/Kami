# Kami
A typical permission management plugin for Nukkit

## Build
- git clone https://github.com/onebone/Kami && cd Kami
- mvn clean
- mvn package

## Commands

- /addgroup `[[-d]]` `<group>`
 - `-d`: Set as default group
- /rmgroup `<group>`
- /usermod `<[-g <group>][-a <permission>][-r <permission>]>` `<player>`
 - `-g`: Set group of player
 - `-a`: Appends permission to player
 - `-r`: Removes permission from player
- /groups
- /user `<player>`
- /group `<[-d][-a <permission>][-r <permission>]>` `<group>`
 - `-d`: Set as default group
- /perms `<[-u <user>][-g <group>][-p <page]>`
 - `-u`: Shows user permission
 - `-g`: Shows group permission
 - `-p`: Sets page to show

## Permissions
- kami
  - kami.command
    - kami.command.addgroup
	- kami.command.rmgroup
	- kami.command.usermod
	- kami.command.groups
	- kami.command.user
  - kami.invalidmove
