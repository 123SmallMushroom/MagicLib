package top.hendrixshen.magiclib.impl.mixin.common.carpet;

import carpet.settings.ParsedRule;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//#if MC > 11802
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#elseif MC <= 11605
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$ import top.hendrixshen.magiclib.api.rule.RuleHelper;
//#else
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif
import top.hendrixshen.magiclib.MagicLibReference;
import top.hendrixshen.magiclib.api.rule.Validators;
import top.hendrixshen.magiclib.api.rule.WrapperSettingManager;
import top.hendrixshen.magiclib.api.rule.annotation.Command;
import top.hendrixshen.magiclib.api.rule.annotation.Numeric;
import top.hendrixshen.magiclib.api.rule.annotation.Rule;
import top.hendrixshen.magiclib.compat.annotation.InitMethod;
import top.hendrixshen.magiclib.compat.annotation.Public;
import top.hendrixshen.magiclib.compat.minecraft.network.chat.ComponentCompatApi;
import top.hendrixshen.magiclib.util.MessageUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ParsedRule.class)
public abstract class MixinParsedRule<T> {
    // All field are dummy.
    public Field field;
    public String name;
    public String description;
    String scarpetApp;
    //#if MC > 11605
    List<String> categories;
    List<String> options;
    //#else
    //$$ ImmutableList<String> categories;
    //$$ ImmutableList<String> options;
    //#endif
    public Class<?> type;
    //#if MC > 11802
    public List<carpet.api.settings.Validator<?>> realValidators;
    //#endif
    public List<carpet.settings.Validator<?>> validators;
    public T defaultValue;
    public String defaultAsString;
    public boolean isStrict;
    public carpet.settings.SettingsManager settingsManager;
    //#if MC > 11802
    public carpet.api.settings.SettingsManager realSettingsManager;
    //#endif

    public Class<?> typeCompat;

    @Shadow
    public abstract T get();

    //#if MC > 11802
    @Shadow
    protected abstract void set(CommandSourceStack par1, Object par2, String par3) throws carpet.api.settings.InvalidRuleValueException;

    @Shadow
    public abstract Class<T> type();
    //#else
    //$$ @Shadow
    //$$ abstract ParsedRule<T> set(CommandSourceStack source, T value, String stringValue);
    //#endif

    @Public
    @InitMethod
    private void magicInit(@NotNull Field field, @NotNull Rule rule, WrapperSettingManager settingsManager) {
        this.field = field;
        this.name = field.getName();
        //#if MC > 11802
        this.type = ClassUtils.primitiveToWrapper(field.getType());
        //#else
        //$$ this.type = field.getType();
        //#endif
        this.typeCompat = ClassUtils.primitiveToWrapper(field.getType());
        this.description = null;
        this.defaultAsString = this.magiclib$convertToString(this.get());
        this.isStrict = rule.strict();
        //#if MC > 11605
        this.categories = Arrays.asList(rule.categories());
        //#else
        //$$ this.categories = ImmutableList.copyOf(rule.categories());
        //#endif
        this.scarpetApp = rule.appSource();
        //#if MC > 11802
        this.settingsManager = null;
        this.realSettingsManager = settingsManager;
        this.validators = Collections.emptyList();
        this.realValidators = Stream.of(rule.validators()).map(this::magiclib$instantiateValidator).collect(Collectors.toList());
        //#else
        //$$ this.validators = Stream.of(rule.validators()).map(this::magiclib$instantiateValidator).collect(Collectors.toList());
        //$$ this.settingsManager = settingsManager;
        //#endif
        this.defaultValue = this.get();

        Command commandAnnotation = field.getAnnotation(Command.class);
        Numeric numericAnnotation = field.getAnnotation(Numeric.class);
        if (rule.options().length > 0) {
            //#if MC > 11605
            this.options = Arrays.asList(rule.options());
            //#else
            //$$ this.options = ImmutableList.copyOf(rule.options());
            //#endif
        } else if (this.typeCompat == Boolean.class) {
            this.isStrict = false;
            //#if MC > 11605
            this.options = Arrays.asList("true", "false");
            //#else
            //$$ this.options = ImmutableList.of("true", "false");
            //#endif
            //#if MC > 11802
            this.realValidators.add(0, new Validators.StrictIgnoreCase<>());
            //#else
            //$$ this.validators.add(0, new Validators.StrictIgnoreCase<>());
            //#endif
        } else if (this.typeCompat.isEnum()) {
            this.isStrict = false;
            this.options = Arrays.stream(this.type.getEnumConstants())
                    .map(e -> ((Enum<?>)e).name().toLowerCase(Locale.ROOT))
                    .collect(ImmutableList.toImmutableList());
        } else if (this.typeCompat == String.class && commandAnnotation != null) {
            this.isStrict = false;
            //#if MC > 11605
            this.options = commandAnnotation.full() ? Validators.Command.FULL_OPTIONS : Validators.Command.MINIMAL_OPTIONS;
            //#else
            //$$ this.options = commandAnnotation.full() ? ImmutableList.copyOf(Validators.Command.FULL_OPTIONS) : ImmutableList.copyOf(Validators.Command.MINIMAL_OPTIONS);
            //#endif
            //#if MC > 11802
            this.realValidators.add(this.magiclib$instantiateValidator(Validators.Command.class));
            //#else
            //$$ this.validators.add(this.magiclib$instantiateValidator(Validators.Command.class));
            //#endif
        } else {
            //#if MC > 11605
            this.options = Collections.emptyList();
            //#else
            //$$ this.options = ImmutableList.of();
            //#endif
        }

        if (numericAnnotation != null && (this.typeCompat == Byte.class || this.typeCompat == Short.class ||
                this.typeCompat == Integer.class || this.typeCompat == Long.class || this.typeCompat == Float.class ||
                this.typeCompat == Double.class)) {
            //#if MC > 11802
            this.realValidators.add(0, new Validators.Numeric<>(numericAnnotation.maxValue(), numericAnnotation.minValue(), numericAnnotation.canMaxEquals(), numericAnnotation.canMinEquals()));
            //#else
            //$$ this.validators.add(0, new Validators.Numeric<>(numericAnnotation.maxValue(), numericAnnotation.minValue(), numericAnnotation.canMaxEquals(), numericAnnotation.canMinEquals()));
            //#endif
        }

        if (this.isStrict && !this.options.isEmpty()) {
            //#if MC > 11802
            this.realValidators.add(0, new Validators.Strict<>());
            //#else
            //$$ this.validators.add(0, new Validators.Strict<>());
            //#endif
        }
    }

    @SuppressWarnings({"rawtypes"})
    private top.hendrixshen.magiclib.api.rule.Validator<?> magiclib$instantiateValidator(Class<? extends top.hendrixshen.magiclib.api.rule.Validator> cls) {
        try {
            Constructor<? extends top.hendrixshen.magiclib.api.rule.Validator> constructor = cls.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private String magiclib$convertToString(Object value) {
        return value instanceof Enum ? ((Enum)value).name().toLowerCase(Locale.ROOT) : value.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(
            //#if MC > 11802
            method = "set(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V",
            //#else
            //$$ method = "set(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)Lcarpet/settings/ParsedRule;",
            //#endif
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    //#if MC > 11802
    public void set(CommandSourceStack source, String value, CallbackInfo ci) throws carpet.api.settings.InvalidRuleValueException {
        if (!(this.realSettingsManager instanceof WrapperSettingManager)) {
            return;
        }
        if (this.typeCompat == String.class) {
            this.set(source, (T) value, value);
        } else if (this.typeCompat == Boolean.class) {
            this.set(source, (T) (Object) Boolean.parseBoolean(value), value);
        } else if (this.typeCompat == Integer.class) {
            this.set(source, (T) (Object) Integer.parseInt(value), value);
        } else if (this.typeCompat == Double.class) {
            this.set(source, (T) (Object) Double.parseDouble(value), value);
        } else if (this.typeCompat.isEnum()) {
            String ucValue = value.toUpperCase(Locale.ROOT);
            try {
                this.set(source, (T) (Object) Enum.valueOf((Class<? extends Enum>) type, ucValue), value);
            } catch (IllegalArgumentException e) {
                MessageUtil.sendMessage(source, ComponentCompatApi.literal(MagicLibReference.getSettingManager().trUI("enum_exception", ucValue))
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
                throw new carpet.api.settings.InvalidRuleValueException();
            }
        } else {
            MessageUtil.sendMessage(source, ComponentCompatApi.literal(MagicLibReference.getSettingManager().trUI("unknown_type", this.typeCompat))
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            throw new carpet.api.settings.InvalidRuleValueException();
        }
        ci.cancel();
    //#else
    //$$ public void set(CommandSourceStack source, String value, CallbackInfoReturnable<ParsedRule<T>> cir) {
    //#else
    //$$     if (!(this.settingsManager instanceof WrapperSettingManager)) {
    //$$         return;
    //$$     }
    //$$     if (this.typeCompat == String.class) {
    //$$         cir.setReturnValue(this.set(source, (T) value, value));
    //$$     } else if (this.typeCompat == Boolean.class) {
    //$$         cir.setReturnValue(this.set(source, (T) (Object) Boolean.parseBoolean(value), value));
    //$$     } else if (this.typeCompat == Integer.class) {
    //$$         cir.setReturnValue(this.set(source, (T) (Object) Integer.parseInt(value), value));
    //$$     } else if (this.typeCompat == Double.class) {
    //$$         cir.setReturnValue(this.set(source, (T) (Object) Double.parseDouble(value), value));
    //$$     } else if (this.typeCompat.isEnum()) {
    //$$         String ucValue = value.toUpperCase(Locale.ROOT);
    //$$         try {
    //$$             cir.setReturnValue(this.set(source, (T) (Object) Enum.valueOf((Class<? extends Enum>) type, ucValue), value));
    //$$         } catch (IllegalArgumentException e) {
    //$$             MessageUtil.sendMessage(source, ComponentCompatApi.literal(MagicLibReference.getSettingManager().trUI("enum_exception", ucValue))
    //$$                     .withStyle(style -> style.withColor(ChatFormatting.RED)));
    //$$             cir.setReturnValue(null);
    //$$         }
    //$$     } else {
    //$$         MessageUtil.sendMessage(source, ComponentCompatApi.literal(MagicLibReference.getSettingManager().trUI("unknown_type", this.typeCompat))
    //$$                 .withStyle(style -> style.withColor(ChatFormatting.RED)));
    //$$         cir.setReturnValue(null);
    //$$     }
    //#endif
    }

    //#if MC <= 11605
    //$$ @Inject(
    //$$         method = "set(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/Object;Ljava/lang/String;)Lcarpet/settings/ParsedRule;",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lcarpet/utils/Messenger;m(Lnet/minecraft/commands/CommandSourceStack;[Ljava/lang/Object;)V"
    //$$         ),cancellable = true
    //$$ )
    //$$ private void onSetFailed(CommandSourceStack source, Object value, String userInput, @NotNull CallbackInfoReturnable<T> cir) {
    //$$     try {
    //$$         RuleHelper.getSettingManager((ParsedRule<?>) (Object) this);
    //$$         cir.setReturnValue(null);
    //$$     } catch (IllegalArgumentException ignore) {
    //$$     }
    //$$ }
    //#endif
}
