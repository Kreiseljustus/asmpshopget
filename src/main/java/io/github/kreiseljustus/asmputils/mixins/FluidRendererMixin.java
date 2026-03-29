package io.github.kreiseljustus.asmputils.mixins;

import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.core.modules.WaterModule;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {

    @Shadow
    private Sprite[] waterSprites;

    private FluidState capturedFluidState;

    @Inject(method = "render", at = @At("HEAD"))
    private void captureFluidState(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer,
                                   BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        this.capturedFluidState = fluidState;
    }

    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/FluidRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFI)V"),
            index = 2
    )
    private float shiftVertexY(float y) {
        if (!WaterModule.s_Enabled) return y;
        if (capturedFluidState == null) return y;
        if (!capturedFluidState.isIn(FluidTags.WATER)) return y;

        float extra = Asmputils.s_Config.extraWaterHeight;
        float fractional = y - (float) Math.floor(y);
        boolean isBottomEdge = fractional < 0.01F;

        if (isBottomEdge) return y;
        return y + extra;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderExtendedSides(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer,
                                     BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (!WaterModule.s_Enabled) return;
        if (!fluidState.isIn(FluidTags.WATER)) return;

        if (world.getFluidState(pos.up()).isIn(FluidTags.WATER)) return;

        float extra = Asmputils.s_Config.extraWaterHeight;
        if (extra <= 0) return;

        float x = pos.getX() & 15;
        float y = pos.getY() & 15;
        float z = pos.getZ() & 15;

        float waterHeight = fluidState.getHeight();
        float bottom = y + waterHeight;
        float top = bottom + extra;

        int color = BiomeColors.getWaterColor(world, pos);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        int light = WorldRenderer.getLightmapCoordinates(world, pos);

        Sprite sprite = waterSprites[1];

        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (world.getFluidState(pos.offset(dir)).isIn(FluidTags.WATER)) continue;

            BlockPos elevatedNeighbor = pos.offset(dir).up((int) extra);
            BlockState elevatedState = world.getBlockState(elevatedNeighbor);
            if (elevatedState.isOpaqueFullCube()) continue;

            float x1, z1, x2, z2;
            switch (dir) {
                case NORTH -> { x1 = x;       z1 = z;        x2 = x + 1f; z2 = z; }
                case SOUTH -> { x1 = x + 1f;  z1 = z + 1f;  x2 = x;      z2 = z + 1f; }
                case WEST  -> { x1 = x;       z1 = z + 1f;  x2 = x;      z2 = z; }
                default    -> { x1 = x + 1f;  z1 = z;       x2 = x + 1f; z2 = z + 1f; }
            }

            float u0 = sprite.getFrameU(0.0F);
            float u1 = sprite.getFrameU(0.5F);
            float v0 = sprite.getFrameV(0.0F);
            float v1 = sprite.getFrameV(0.5F);

            float brightness = dir.getAxis() == Direction.Axis.Z
                    ? world.getBrightness(Direction.NORTH, true)
                    : world.getBrightness(Direction.WEST, true);

            float br = brightness * r;
            float bg = brightness * g;
            float bb = brightness * b;

            // Front face
            vertexConsumer.vertex(x1, top,    z1).color(br, bg, bb, 1.0f).texture(u0, v0).light(light).normal(0, 1, 0);
            vertexConsumer.vertex(x2, top,    z2).color(br, bg, bb, 1.0f).texture(u1, v0).light(light).normal(0, 1, 0);
            vertexConsumer.vertex(x2, bottom, z2).color(br, bg, bb, 1.0f).texture(u1, v1).light(light).normal(0, 1, 0);
            vertexConsumer.vertex(x1, bottom, z1).color(br, bg, bb, 1.0f).texture(u0, v1).light(light).normal(0, 1, 0);
            // Back face
            vertexConsumer.vertex(x1, bottom, z1).color(br, bg, bb, 1.0f).texture(u0, v1).light(light).normal(0, 1, 0);
            vertexConsumer.vertex(x2, bottom, z2).color(br, bg, bb, 1.0f).texture(u1, v1).light(light).normal(0, 1, 0);
            vertexConsumer.vertex(x2, top,    z2).color(br, bg, bb, 1.0f).texture(u1, v0).light(light).normal(0, 1, 0);
            vertexConsumer.vertex(x1, top,    z1).color(br, bg, bb, 1.0f).texture(u0, v0).light(light).normal(0, 1, 0);
        }
    }
}