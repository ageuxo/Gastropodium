package io.github.ageuxo.Gastropodium.entity.pathing;

import io.github.ageuxo.Gastropodium.entity.BasicSlugEntity;
import net.minecraft.core.Direction;
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

            if (this.operation == Operation.MOVE_TO) {
                double deltaX = this.wantedX - this.crawler.getX();
                double deltaY = this.wantedY - this.crawler.getY();
                double deltaZ = this.wantedZ - this.crawler.getZ(); //comment of shame for not noticing I used the wrong var here
                double dSquared = (deltaX * deltaX) + (deltaY * deltaY ) + (deltaZ * deltaZ);
                this.operation = Operation.WAIT;
                float yRot = (float) ((Mth.atan2(deltaZ, deltaX) * (180F/Math.PI))-90F); // TODO make the snail stop spinning in place
                this.crawler.setYRot(yRot);

                if (dSquared < Double.MIN_VALUE){
                    this.crawler.setSpeed(0.0F);
                } else {
                    this.crawler.setSpeed((float) (this.speedModifier * this.crawler.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
            } else {
                this.crawler.setSpeed(0.0F);
            }
        } else {
            this.crawler.setSpeed(0.0F);
        }
    }
}
