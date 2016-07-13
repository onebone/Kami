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
- /perms `<[-u <user>][-g <group>][-p <page>]>`
 - `-u`: Shows user permission
 - `-g`: Shows group permission
 - `-p`: Sets page to show

#### Command Example
- `/addgroup NewGroup` will create a new group named `NewGroup`.
- `/addgroup -d NewGroup` will create a new group named `NewGroup` and set it as default group.
- `/addgroup "With Space"` will create a new group named `With Space`.
- `/usermod -g Admin onebone` will set `onebone`'s group into `Admin`.
- `/usermod -a kami.command.* onebone` will append permission node `kami.command.*` to `onebone`.
- `/group -a kami.command.* Admin` will append permission node `kami.command.*` to `Admin` group.
- `/perms -u onebone -p 1` will show page `1` of `onebone`'s permission node.

## Permission management
You can manually or by command to modify permissions for players.
Let's give an example.

| Permission | Node | Matching |
|:----------:|:----:|:--------:|
| nukkit.command.say | nukkit.command.say | Yes |
| nukkit.command.me | nukkit.command.me | Yes |
| kami.command | kami.* | Yes |
| kami.command.groups | kami.*.groups | Yes |
| kami.command.asdf.groups | kami.*.groups | Yes |
| All Permissions Available | * | Yes |
| kami.invalidmove | kami | No ** |
| nukkit.command | kami | No |

\*\* But will be applied because it is children

- You can negate permission by using prefix `-` at the start of the permission node.
	- Example: `-kami` will negate permission `kami`, and `-*` will negate all permissions available.

## Permissions
- kami
  - kami.command
    - kami.command.addgroup
	- kami.command.rmgroup
	- kami.command.usermod
	- kami.command.groups
	- kami.command.user
  - kami.invalidmove
