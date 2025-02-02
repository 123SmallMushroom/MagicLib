package top.hendrixshen.magiclib.compat.mixin.minecraft.client.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.hendrixshen.magiclib.compat.minecraft.client.gui.FontCompatApi;
import top.hendrixshen.magiclib.compat.minecraft.network.chat.ComponentCompatApi;

//#if MC > 11404
import net.minecraft.client.renderer.MultiBufferSource;
//#else
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//$$ import com.mojang.blaze3d.vertex.BufferBuilder;
//$$ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//$$ import org.lwjgl.opengl.GL11;
//#endif

@Environment(EnvType.CLIENT)
@Mixin(Font.class)
public abstract class MixinFont implements FontCompatApi {
    //#if MC <= 11502
    //$$ @Shadow
    //$$ public abstract int width(String string);
    //#endif

    @Override
    public int widthCompat(Component component) {
        //#if MC > 11502
        return ((Font) (Object) this).width(component);
        //#else
        //$$ return this.width(component.getString());
        //#endif
    }

    //#if MC > 11404 && MC <= 11502
    //$$ @Shadow
    //$$ public abstract int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k);
    //#elseif MC <= 11404
    //$$ @Shadow
    //$$ public abstract int draw(String string, float x, float y, int color);
    //$$ @Shadow
    //$$ public abstract int drawShadow(String string, float x, float y, int color);
    //#endif

    //#if MC > 11502
    @Shadow
    public abstract int drawInBatch(Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k);
    //#endif

    @Override
    public int drawInBatch(Component component, float x, float y, int color, boolean shadow, Matrix4f matrix4f,
                           boolean seeThrough, int backgroundColor, int light) {
        Tesselator tesselator = Tesselator.getInstance();
        //#if MC > 11404
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(tesselator.getBuilder());
        //#if MC > 11502
        int ret = this.drawInBatch(component, x, y, color, shadow, matrix4f, bufferSource, seeThrough, backgroundColor, light);
        //#else
        //$$ int ret = this.drawInBatch(component.getColoredString(), x, y, color, shadow, matrix4f, bufferSource,
        //$$         seeThrough, backgroundColor, light);
        //#endif
        bufferSource.endBatch();
        //#else
        //$$ GlStateManager.pushMatrix();
        //$$ GlStateManager.multMatrix(matrix4f);
        //$$ if (seeThrough) {
        //$$     GlStateManager.depthMask(false);
        //$$     GlStateManager.disableDepthTest();
        //$$ }
        //$$
        //$$ GlStateManager.enableBlend();
        //$$ GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        //$$ GlStateManager.disableTexture();
        //$$
        //$$ float a = (float) (backgroundColor >> 24 & 255) / 255.0F;
        //$$ float r = (float) (backgroundColor >> 16 & 255) / 255.0F;
        //$$ float g = (float) (backgroundColor >> 8 & 255) / 255.0F;
        //$$ float b = (float) (backgroundColor & 255) / 255.0F;
        //$$
        //$$ BufferBuilder bufferBuilder = tesselator.getBuilder();
        //$$ bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
        //$$ bufferBuilder.vertex(x - 1, y - 1, 0).color(r, g, b, a).endVertex();
        //$$ bufferBuilder.vertex(x - 1, y + 8, 0).color(r, g, b, a).endVertex();
        //$$ bufferBuilder.vertex(x + this.width(component) + 1, y + 8, 0).color(r, g, b, a).endVertex();
        //$$ bufferBuilder.vertex(x + this.width(component) + 1, y - 1, 0).color(r, g, b, a).endVertex();
        //$$ tesselator.end();
        //$$ GlStateManager.enableTexture();
        //$$ int ret;
        //$$ if (shadow) {
        //$$     ret = this.drawShadow(component.getColoredString(), x, y, color);
        //$$ } else {
        //$$     ret = this.draw(component.getColoredString(), x, y, color);
        //$$ }
        //$$ if (seeThrough) {
        //$$     GlStateManager.depthMask(true);
        //$$     GlStateManager.enableDepthTest();
        //$$ }
        //$$ GlStateManager.disableBlend();
        //$$ GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //$$ GlStateManager.popMatrix();
        //#endif
        return ret;
    }

    @Override
    public int drawInBatch(String text, float x, float y, int color, boolean shadow, Matrix4f matrix4f,
                           boolean seeThrough, int backgroundColor, int light) {
        return this.drawInBatch(ComponentCompatApi.literal(text), x, y, color, shadow,
                matrix4f, seeThrough, backgroundColor, light);
    }
}
