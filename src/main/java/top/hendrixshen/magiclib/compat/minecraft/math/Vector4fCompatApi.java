package top.hendrixshen.magiclib.compat.minecraft.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public interface Vector4fCompatApi {
    default void transformCompat(Matrix4f matrix4f) {
        throw new UnsupportedOperationException();
    }

    //#if MC <= 11404
    //$$ default void transform(Matrix4f matrix4f) {
    //$$     this.transformCompat(matrix4f);
    //$$ }
    //#endif
}
