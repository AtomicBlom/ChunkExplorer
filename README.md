XRayForModders
==============

About
-----
This mod is specifically for Forge Mod developers, as I was unable to get any coremod XRay mod that I could find working inside of IntelliJ. It is dangerously lethal on worlds and should be used only as a debugging tool for world generation. I will not provide support for anybody using this on a production world.

This is a simple forge mod that adds a set of utility commands:

```/bvxray [radiusInChunks] [filter]```

```/bvregen [radiusInChunks]```

```/bvsurvey [radiusInChunks] [filter]```

```/bvreplace [radiusInChunks] blockToReplace blockToReplaceWith```


/bvxray
-------
```/bvxray [radiusInChunks] [filter]```

This command will destructively remove a set of blocks in a square chunk radius from your world, allowing you to see the ore underneath.

The blocks removed are:
* Stone
* Sand
* Grass (block)
* Gravel
* Dirt

In order to prevent massive world lag, it will not remove any block that is touching Lava or Water.

You can specify a filter to limit the blocks being removed. It uses a partial match, but is case sensitive, so typing 'stone' will not match 'sandStone'

Although there is no limit, attempting to remove too many chunks worth of blocks will result in the server and then the client stalling while it processes your command.

/bvregen
--------
```/bvregen [radiusInChunks]```

This command will cause the chunk to be regenerated. Depending on the implementations of the generation you may not end up with a 100% identical chunk. This is especially true around the edges as the order that the chunks are created can have an impact on ore generation.

/bvsurvey
---------
```/bvsurvey [radiusInChunks] [filter]```

This command will scan all of the blocks in a chunk and give you a report on the various blocks found in a chunk.

You can specify a filter to limit the blocks being surveyed.

/bvreplace
----------
```/bvreplace [radiusInChunks] blockToReplace blockToReplaceWith```

A ridiculous addition my son asked me to add.

Allows you to replace one type of block with another.

Distribution
------------
This code is complete in that it compiles and does what it advertises on the tin, but I have not yet investigated distribution channels so I have not compiled a binary for downloading.

Running the mod from source
---------------------------
Grab a copy of the code and open up a shell prompt where you have put the files.

Execute the following:
``` gradlew build```
 
you will be able to get a copy of the .jar from XRayForModders\build\libs\\[1.7.10]xrayformodders-deobf-0.1.jar

Place this jar in the directory in your development environment and run it.

The Name
--------
Just to clarify, I know this is not XRay, in the same vein that a cup that once contained Milk isn't filled with transparent milk just because you drank it, but the visual result is what I was after.
