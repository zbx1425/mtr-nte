package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import cn.zbx1425.mtrsteamloco.render.rail.RailRenderDispatcher;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.shader.ShaderManager;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.reuse.ModelManager;

public class MainClient {

	public static ShaderManager shaderManager = new ShaderManager();
	public static BatchManager batchManager = new BatchManager();
	public static ModelManager modelManager = new ModelManager();
	public static AtlasManager atlasManager = new AtlasManager();

	public static RailRenderDispatcher railRenderDispatcher = new RailRenderDispatcher();

	public static void init() {
		ClientConfig.load();
		ShadersModHandler.init();

		// RegistryClient.registerBlockRenderType(RenderType.cutout(), Main.BLOCK_STATISTIC_TURNSTILE.get());

		// mtr.RegistryClient.registerNetworkReceiver(PacketFeedback.PACKET_FEEDBACK, PacketFeedback::receiveFeedbackS2C);
	}

}
