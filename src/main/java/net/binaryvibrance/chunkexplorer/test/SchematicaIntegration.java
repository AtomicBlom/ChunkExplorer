package net.binaryvibrance.chunkexplorer.test;

import com.github.lunatrius.schematica.api.PreSchematicSaveEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Optional;

public class SchematicaIntegration
{
    public static final SchematicaIntegration INSTANCE = new SchematicaIntegration();

    @Mod.EventHandler
    @Optional.Method(modid = "Schematica")
    public void onPreSchematicSave(PreSchematicSaveEvent event) {

    }
}
