
function DisplayHelper(cfg) {
    if (cfg === void 0) return;

    this.cfg = cfg;
    this.texture = null;
    this.ownsTexture = false;
    if (cfg.version === 1) {
        let renderType = cfg.renderType || "interior";
        let meshBuilder = new RawMeshBuilder(4, renderType, Resources.id("minecraft:textures/misc/white.png"));
        meshBuilder.color(255, 255, 255, 255);
        for (let slotCfg of cfg.slots) {
            let realUV = Array(4);
            realUV[0] = [slotCfg.texArea[0] / cfg.texSize[0],
                slotCfg.texArea[1] / cfg.texSize[1]];
            realUV[1] = [slotCfg.texArea[0] / cfg.texSize[0],
                (slotCfg.texArea[1] + slotCfg.texArea[3]) / cfg.texSize[1]];
            realUV[2] = [(slotCfg.texArea[0] + slotCfg.texArea[2]) / cfg.texSize[0],
                (slotCfg.texArea[1] + slotCfg.texArea[3]) / cfg.texSize[1]];
            realUV[3] = [(slotCfg.texArea[0] + slotCfg.texArea[2]) / cfg.texSize[0],
                slotCfg.texArea[1] / cfg.texSize[1]];

            if (slotCfg.offsets === void 0) slotCfg.offset = [[0, 0, 0]];
            for (let offset of slotCfg.offsets) {
                for (let posCfg of slotCfg.pos) {
                    for (let i = 0; i < 4; i++) {
                        meshBuilder
                            .vertex(posCfg[i][0] + offset[0], posCfg[i][1] + offset[1], posCfg[i][2] + offset[2])
                            .normal(0, 1, 0)
                            .uv(realUV[i][0], realUV[i][1])
                            .endVertex();
                    }
                }
            }
        }

        let rawModel = new RawModel();
        rawModel.append(meshBuilder.getMesh());
        rawModel.triangulate();
        this.baseModel = ModelManager.uploadVertArrays(rawModel);
    } else {
        throw new Error("Unknown version: " + cfg.version);
    }
}

DisplayHelper.prototype.create = function(sharedTexture) {
    let instance = new DisplayHelper();
    if (this.cfg.version === 1) {
        if (sharedTexture !== void 0) {
            instance.texture = sharedTexture;
            instance.ownsTexture = false;
        } else {
            instance.texture = new GraphicsTexture(this.cfg.texSize[0], this.cfg.texSize[1]);
            instance.ownsTexture = true;
        }
        instance._graphics = instance.texture.graphics;

        instance.emptyTransform = instance._graphics.getTransform();
        instance.slotTransforms = {};
        for (let slotCfg of this.cfg.slots) {
            instance._graphics.transform(java.awt.geom.AffineTransform.getTranslateInstance(slotCfg.texArea[0], slotCfg.texArea[1]));
            if (slotCfg.paintingSize !== void 0) {
                instance._graphics.transform(java.awt.geom.AffineTransform.getScaleInstance(slotCfg.texArea[2] / slotCfg.paintingSize[0],
                    slotCfg.texArea[4] / slotCfg.paintingSize[1]));
            }
            instance.slotTransforms[slotCfg.name] = instance._graphics.getTransform();
            instance._graphics.setTransform(instance.emptyTransform);
        }

        instance.model = this.baseModel.copyForMaterialChanges();
        instance.model.replaceAllTexture(instance.texture.identifier);
    } else {
        throw new Error("Unknown version: " + cfg.version);
    }
    return instance;
}

DisplayHelper.prototype.upload = function() {
    if (this.ownsTexture) {
        this.texture.upload();
    }
}

DisplayHelper.prototype.close = function() {
    if (this.ownsTexture) {
        this.texture.close();
    }
}

DisplayHelper.prototype.graphics = function() {
    this._graphics.setTransform(this.emptyTransform);
    return this._graphics;
}

DisplayHelper.prototype.graphicsFor = function(slotName) {
    this._graphics.setTransform(this.slotTransforms[slotName]);
    return this._graphics;
}
