package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.data.DisplayRegistry;
import cn.zbx1425.mtrsteamloco.network.PacketScreenClient;
import cn.zbx1425.mtrsteamloco.network.PacketScreenServer;
import cn.zbx1425.mtrsteamloco.network.PacketVersionCheck;
import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import cn.zbx1425.mtrsteamloco.render.block.BlockEntityEyeCandyRenderer;
import cn.zbx1425.mtrsteamloco.render.rail.RailRenderDispatcher;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.shader.ShaderManager;
import cn.zbx1425.sowcer.util.Profiler;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import mtr.RegistryClient;
import mtr.client.TrainClientRegistry;
import net.minecraft.client.renderer.RenderType;

public class MainClient {

	public static DrawScheduler drawScheduler = new DrawScheduler();
	public static ModelManager modelManager = new ModelManager();
	public static AtlasManager atlasManager = new AtlasManager();

	public static RailRenderDispatcher railRenderDispatcher = new RailRenderDispatcher();

	public static Profiler profiler = new Profiler();

	public static void init() {
		ClientConfig.load();
		ShadersModHandler.init();

		mtr.client.CustomResources.registerReloadListener(CustomResources::init);

		drawScheduler.setImmediateDrawCall(DisplayRegistry::drawAllImmediate);

		if (Main.enableRegistry) {
			RegistryClient.registerTileEntityRenderer(Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get(), BlockEntityEyeCandyRenderer::new);

			RegistryClient.registerNetworkReceiver(PacketVersionCheck.PACKET_VERSION_CHECK, PacketVersionCheck::receiveVersionCheckS2C);
			RegistryClient.registerNetworkReceiver(PacketScreenServer.PACKET_SHOW_SCREEN, PacketScreenClient::receiveBlockEntityScreenS2C);
			// RegistryClient.registerBlockRenderType(RenderType.cutout(), Main.BLOCK_EYE_CANDY.get());

			// RegistryClient.registerBlockRenderType(RenderType.cutout(), Main.BLOCK_STATISTIC_TURNSTILE.get());
		}

		RegistryClient.registerPlayerJoinEvent(localPlayer -> {
			railRenderDispatcher.clearRail();
		});
	}

}
