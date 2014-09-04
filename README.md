XRayForModders
==============

About
-----
This mod is specifically for Forge Mod developers, as I was unable to get any coremod XRay mod that I could find working inside of IntelliJ. It is dangerously lethal on worlds and should be used only as a debugging tool for world generation. I will not provide support for anybody using this on a production world.

This is a simple forge mod that adds a single command:

/bvxray [radius]

This command will destructively remove a set of blocks in a square chunk radius from your world, allowing you to see the ore underneath.

The blocks removed are:
* Stone
* Sand
* Grass (block)
* Gravel
* Dirt

In order to prevent massive world lag, it will not remove any block that is touching Lava or Water.

Although there is no limit, attempting to remove too many chunks worth of blocks will result in the server and then the client stalling while it processes your command.

Distribution
------------
This code is complete in that it compiles and does what it advertises on the tin, but I have not yet investigated distribution channels so I have not compiled a binary for downloading.

Running the mod from source
---------------------------
Grab a copy of the code and open up a shell prompt where you have put the files.

Execute the following:
``` gradlew build```
 
you will be able to get a copy of the .jar from XRayForModders\build\libs\[1.7.10]xrayformodders-deobf-0.1.jar

Place this jar in the directory in your development environment and run it.

The Name
--------
Just to clarify, I know this is not XRay, in the same vein that a cup that once contained Milk isn't filled with transparent milk just because you drank it, but the visual result is what I was after.
