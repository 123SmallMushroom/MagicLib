package top.hendrixshen.magiclib.impl.mixin.client.malilib;

import org.spongepowered.asm.mixin.Mixin;
//#if MC > 11701
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.config.TranslatableConfigBooleanHotkeyed;
import top.hendrixshen.magiclib.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.dependency.annotation.Dependency;

@Dependencies(and = @Dependency(value = "malilib", versionPredicate = ">=0.11.4"))
@Mixin(value = WidgetConfigOption.class, remap = false)
public abstract class MixinWidgetConfigOptionForBooleanHotkeyedModern extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    @Shadow
    @Final
    protected IKeybindConfigGui host;

    public MixinWidgetConfigOptionForBooleanHotkeyedModern(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }

    @Inject(
            method = "addConfigOption",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addBooleanAndHotkeyWidgets(IIILfi/dy/masa/malilib/config/IConfigResettable;Lfi/dy/masa/malilib/config/IConfigBoolean;Lfi/dy/masa/malilib/hotkeys/IKeybind;)V",
                    ordinal = 1
            ),
            cancellable = true
    )
    private void tweakConfigBooleanHotkeyedElements(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci) {
        if (config instanceof TranslatableConfigBooleanHotkeyed) {
            TranslatableConfigBooleanHotkeyed hotkeyedBoolean = (TranslatableConfigBooleanHotkeyed) config;
            IKeybind keybind = hotkeyedBoolean.getKeybind();
            this.magiclib$addConfigBooleanHotkeyedElements(x, y, configWidth, hotkeyedBoolean, hotkeyedBoolean, keybind);
            ci.cancel();
        }
    }

    private void magiclib$addConfigBooleanHotkeyedElements(int x, int y, int configWidth, IConfigResettable resettableConfig, IConfigBoolean booleanConfig, IKeybind keybind) {
        int booleanBtnWidth = (configWidth - 24) / 2;
        ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, booleanConfig);
        x += booleanBtnWidth + 2;

        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, booleanBtnWidth, 20, keybind, this.host);
        x += booleanBtnWidth + 2;

        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, booleanConfig.getName(), this.parent, this.host.getDialogHandler()));
        x += 22;
        ButtonGeneric resetButton = this.createResetButton(x, y, resettableConfig);
        ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(resettableConfig, resetButton, null);
        WidgetConfigOption.HotkeyedBooleanResetListener resetListener = new WidgetConfigOption.HotkeyedBooleanResetListener(
                resettableConfig, booleanButton, keybindButton, resetButton, this.host);
        this.host.addKeybindChangeListener(resetListener::updateButtons);
        this.addButton(booleanButton, booleanChangeListener);
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, resetListener);
    }
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ @Mixin(Minecraft.class)
//$$ public class MixinWidgetConfigOptionForBooleanHotkeyedModern {
//#endif
}
