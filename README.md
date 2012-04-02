# Gates

## Features

* Create gates using Nether Fence, Fence, Iron Fence or Thin Glass.
* Gates can't be harmed, preventing duplication.
* Only those with permission can use your gates.
* Gates have open/close animations, one row of blocks at a time.

## How to make a gate?

1. Place a sign.
2. Write [Gate] (case-insensitive) on one of the lines, which line doesn't matter.
3. Right click the gate.
4. If there are any blocks of the material stated above nearby, the gate will automatically detect itself.

### Things to take note of during gate creation:

* If no blocks were found it's advised to just break the sign as it wont do another search, unless you've changed the configurations to search after blocks on gate use.
* Just one sign each gate, unless you've changed the configurations to allow several signs for one gate.
* Redstone will not trigger the gate sign, because I can't check the permission of the player who triggered the redstone. I will look into the best solution possible.
* Remove all blocks, including snow and flowers, which will obstruct the gate leaving a odd looking hole.
* After the gate is created, placing or breaking blocks which is a part of the gate is impossible, thus make sure you're finished with all that before registering the sign.

### Thins to take note of during destroying a gate:

* You can't destroy a gate while it's opening/closing.
* Once destroyed, you can edit the blocks which were just before protected.
* All gate blocks will be removed, except the topmost. This is to prevent duplication, even though it might remove some of the user placed blocks.

## Redstone?

No, not yet. I will try to find a solution.

## Commands

/gates reload - Reload the configurations and gates  
/gates version - Display the version of the plugin

## Permissions

### > gates.*
**Description**: Grant full access to the plugin  
**Default**: op

### > > gates.create
**Description**: Grant access to create gates  
**Default**: true

### > > gates.use.*
**Description**: Grant access to use all gates

### > > > gates.use.self
**Description**: Grant access to use gates made by yourself  
**Default**: true

### > > > gates.use.others
**Description**: Grant access to use gates made by other players

### > > gates.destroy.*
**Description**: Grant access to destroy all gates

### > > > gates.destroy.self
**Description**: Grant access to destroy gates made by yourself  
**Default**: true

### > > > gates.destroy.others
**Description**: Grant access to destroy gates made by other players

### > > gates.command.*
**Description**: Grant full access to the /gates command

### > > > gates.command.reload
**Description**: Grant access to reload the plugin using the /gates command

### > > > gates.command.version
**Description**: Grant access to display the version of the plugin using the /gates command  
**Default**: true

## Thanks To

* Falsebook/Craftbook for the initial idea
* KiteCraft in general

