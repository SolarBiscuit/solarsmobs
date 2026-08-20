package com.solarbiscuit.client.femboy;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public class FemboyModel extends HumanoidModel<FemboyEntity> {
    public FemboyModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(FemboyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean sitting = entity.isInSittingPose();
        this.crouching = entity.isCrouching() && !sitting;
        this.riding = sitting || entity.isPassenger();
        super.setupAnim(entity, sitting ? 0.0F : limbSwing, sitting ? 0.0F : limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (sitting) {
            this.body.xRot = 0.2F;
            this.rightArm.xRot += 0.2F;
            this.leftArm.xRot += 0.2F;
        }
    }
}
