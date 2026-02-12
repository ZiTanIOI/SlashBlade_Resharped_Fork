package mods.flammpfeil.slashblade.client.renderer.model;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Furia on 2016/02/06.
 */
public class BladeMotionManager {

    private static final class SingletonHolder {
        private static final BladeMotionManager instance = new BladeMotionManager();
    }

    public static BladeMotionManager getInstance() {
        return SingletonHolder.instance;
    }

    private BladeMotionManager() {
        // 移除了对jp.nyatla.nymmd包的依赖
    }

    @SubscribeEvent
    public void reload(TextureStitchEvent.Post event) {
        // 移除了对jp.nyatla.nymmd包的依赖
    }

    public Object getMotion(ResourceLocation loc) {
        // 移除了对jp.nyatla.nymmd包的依赖，返回null
        return null;
    }

}






