package buildcraft.api.bpt;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.IUniqueReader;
import buildcraft.api.bpt.helper.ActionSetBlockState;
import buildcraft.api.bpt.helper.BptTaskBlockClear;
import buildcraft.api.bpt.helper.BptTaskBlockStandalone;

public class BlueprintAPI {
    private static final Map<ResourceLocation, IBptTaskDeserializer> taskDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, IUniqueReader<IBptAction>> actionDeserializers = new HashMap<>();

    public static void registerTaskDeserializer(ResourceLocation identifier, IBptTaskDeserializer deserializer) {
        taskDeserializers.put(identifier, deserializer);
    }

    public static IBptTaskDeserializer getTaskDeserializer(ResourceLocation identifier) {
        return taskDeserializers.get(identifier);
    }

    public static void registerActionDeserializer(ResourceLocation identifier, IUniqueReader<IBptAction> deserializer) {
        actionDeserializers.put(identifier, deserializer);
    }

    public static IUniqueReader<IBptAction> getActionDeserializer(ResourceLocation identifier) {
        return actionDeserializers.get(identifier);
    }

    static {
        // Default task deserializers
        registerTaskDeserializer(BptTaskBlockStandalone.ID, BptTaskBlockStandalone.Deserializer.INSTANCE);
        registerTaskDeserializer(BptTaskBlockClear.ID, BptTaskBlockClear.Deserializer.INSTANCE);

        // Default action deserializers
        registerActionDeserializer(ActionSetBlockState.ID, ActionSetBlockState.Deserializer.INSTANCE);
    }
}