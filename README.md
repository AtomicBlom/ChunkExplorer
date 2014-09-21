Chunk Explorer
==============

About
-----
This mod is specifically for Forge Mod developers. It provides a collection of tools that are useful for debugging world generation related mods.

This is a simple forge mod that adds a set of utility commands:

```/chunkxray [radiusInChunks] [filter]```

```/chunkregen [radiusInChunks]```

```/chunksurvey [radiusInChunks] [filter]```

```/chunkreplace [radiusInChunks] blockToReplace blockToReplaceWith```


/chunkxray
-------
```/chunkxray [radiusInChunks] [filter]```

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

/chunkregen
--------
```/chunkregen [radiusInChunks]```

This command will cause the chunk to be regenerated. Depending on the implementations of the generation you may not end up with a 100% identical chunk. This is especially true around the edges as the order that the chunks are created can have an impact on ore generation.

/chunksurvey
---------
```/chunksurvey [radiusInChunks] [filter]```

This command will scan all of the blocks in a chunk and give you a report on the various blocks found in a chunk.

You can specify a filter to limit the blocks being surveyed.

/chunkreplace
----------
```/chunkreplace [radiusInChunks] blockToReplace blockToReplaceWith```

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
 
you will be able to get a copy of the .jar from ChunkExplorer\build\libs\\[1.7.10]chunkexplorer-1.0.jar

Place this jar in the directory in your development environment and run it.
