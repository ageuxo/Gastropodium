package io.github.ageuxo.Gastropodium.entity.pathing;

import io.github.ageuxo.Gastropodium.entity.BasicSlugEntity;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class BlockEdgeCrawlerMoveControl extends MoveControl {
    private final BasicSlugEntity crawler;

    public BlockEdgeCrawlerMoveControl(BasicSlugEntity pMob) {
        super(pMob);
        this.crawler = pMob;
    }

    @Override
    public void tick() {

        if (this.crawler.isAttached()) {
            double deltaX = this.wantedX - this.crawler.getX();
            double deltaY = this.wantedY - this.crawler.getY();
            double deltaZ = this.wantedX - this.crawler.getZ();


            if (this.operation == Operation.MOVE_TO) {
                this.crawler.setNoGravity(true);
                //IDK do math I guess
                float wantedAngleY = (float) (Mth.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90.0F;
//                    this.crawler.setYRot(this.rotlerp(this.crawler.getYRot(), wantedAngleY, 360));
                this.crawler.setYRot(wantedAngleY % 90);
//                float wantedAngleX = (float) (Mth.atan2(deltaZ, deltaY) * (180 / Math.PI)) - 90.0F;
//                this.mob.setXRot(this.rotlerp(this.mob.getXRot(), wantedAngleX, 45));
                Vec3i vec3i = this.crawler.attachDirection.getNormal();
                this.crawler.setYRot(vec3i.getY());
                this.crawler.setXRot(vec3i.getX());


                this.crawler.setSpeed((float) (this.speedModifier * this.crawler.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }


        } else if (this.crawler.onGround()) {
            if (this.crawler.attach(this.crawler, this.crawler.getOnPos())){
                this.crawler.isAttached = true;
            }
        }

    }
}
