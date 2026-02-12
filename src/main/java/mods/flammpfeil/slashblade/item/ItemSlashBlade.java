package mods.flammpfeil.slashblade.item;

import com.google.common.collect.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.SlashBladeCreativeGroup;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.capability.slashblade.NamedBladeStateCapabilityProvider;
// 移除1.19.2不兼容的导入
import mods.flammpfeil.slashblade.client.renderer.SlashBladeTEISR;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class ItemSlashBlade extends SwordItem {
    protected static final UUID ATTACK_DAMAGE_AMPLIFIER = UUID.fromString("2D988C13-595B-4E58-B254-39BB6FA077FD");
    protected static final UUID PLAYER_REACH_AMPLIFIER = UUID.fromString("2D988C13-595B-4E58-B254-39BB6FA077FE");

    public static final Capability<ISlashBladeState> BLADESTATE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final List<Enchantment> exEnchantment = List.of(Enchantments.SOUL_SPEED, Enchantments.POWER_ARROWS,
            Enchantments.FALL_PROTECTION, Enchantments.FIRE_PROTECTION,
            ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "feather_falling")),
            ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "fire_protection")),
            Enchantments.THORNS);

    public ItemSlashBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (exEnchantment.contains(enchantment)) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {
        return this.getBladeId(itemStack).getNamespace();
    }

    public static class ReachModifier {
        public static double BladeReach() {
            return 0.5;
        }
        public static double BrokendReach() {
            return 0.25;
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> def = super.getAttributeModifiers(slot, stack);
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        result.putAll(Attributes.ATTACK_DAMAGE, def.get(Attributes.ATTACK_DAMAGE));
        result.putAll(Attributes.ATTACK_SPEED, def.get(Attributes.ATTACK_SPEED));

        if (slot == EquipmentSlot.MAINHAND) {
            LazyOptional<ISlashBladeState> state = stack.getCapability(BLADESTATE);
            state.ifPresent(s -> {
                // 刀的状态
                var swordType = SwordType.from(stack);
                // 获得基础攻击力
                float baseAttackModifier = s.getBaseAttackModifier();
                // 锻造数
                int refine = s.getRefine();

                s.getAttackAmplifier();
                float attackAmplifier;
                if (s.isBroken()) {
                    // 断刀-0.5伤害
                    attackAmplifier = -0.5F - baseAttackModifier;
                } else {
                    float refineFactor = swordType.contains(SwordType.FIERCEREDGE) ? 0.1F : 0.05F;
                    // 锻造伤害面板增加计算，非线性，收益递减（理论最大值为额外100%基础攻击）
                    attackAmplifier = (1.0F - (1.0F / (1.0F + (refineFactor * refine)))) * baseAttackModifier;
                }

                double damage = (double) baseAttackModifier + attackAmplifier - 1F;

                var event = new SlashBladeEvent.UpdateAttackEvent(stack, s, damage);
                MinecraftForge.EVENT_BUS.post(event);

                AttributeModifier attack = new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        event.getNewDamage(), AttributeModifier.Operation.ADDITION);

                result.remove(Attributes.ATTACK_DAMAGE, attack);
                result.put(Attributes.ATTACK_DAMAGE, attack);

                result.put(ForgeMod.REACH_DISTANCE.get(),
                        new AttributeModifier(PLAYER_REACH_AMPLIFIER, "Reach amplifer",
                                s.isBroken() ? ReachModifier.BrokendReach() : ReachModifier.BladeReach(),
                                AttributeModifier.Operation.ADDITION));

            });
        }

        return result;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        EnumSet<SwordType> type = SwordType.from(stack);
        if (type.contains(SwordType.BEWITCHED)) {
            return Rarity.EPIC;
        }
        if (type.contains(SwordType.ENCHANTED)) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (handIn == InteractionHand.OFF_HAND && !(playerIn.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
            return InteractionResultHolder.pass(itemstack);
        }
        boolean result = itemstack.getCapability(BLADESTATE).map((state) -> {

            playerIn.getCapability(INPUT_STATE).ifPresent((s) -> s.getCommands().add(InputCommand.R_CLICK));

            ResourceLocation combo = state.progressCombo(playerIn);

            playerIn.getCapability(INPUT_STATE).ifPresent((s) -> s.getCommands().remove(InputCommand.R_CLICK));

            if (!combo.equals(ComboStateRegistry.NONE.getId())) {
                playerIn.swing(handIn);
            }

            return true;
        }).orElse(false);

        playerIn.startUsingItem(handIn);
        return new InteractionResultHolder<>(result ? InteractionResult.SUCCESS : InteractionResult.FAIL, itemstack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemstack, Player playerIn, Entity entity) {
        Optional<ISlashBladeState> stateHolder = itemstack.getCapability(BLADESTATE)
                .filter((state) -> !state.onClick());

        stateHolder.ifPresent((state) -> {
            playerIn.getCapability(INPUT_STATE).ifPresent((s) -> s.getCommands().add(InputCommand.L_CLICK));

            state.progressCombo(playerIn);

            playerIn.getCapability(INPUT_STATE).ifPresent((s) -> s.getCommands().remove(InputCommand.L_CLICK));
        });

        return stateHolder.isPresent();
    }

    public static final String BREAK_ACTION_TIMEOUT = "BreakActionTimeout";

    @Override
    public void setDamage(ItemStack stack, int damage) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage < 0) {
            return;
        }
        var state = stack.getCapability(BLADESTATE).orElseThrow(NullPointerException::new);
        if (state.isBroken()) {
            if (damage <= 0 && !state.isSealed()) {
                state.setBroken(false);
            } else if (maxDamage < damage) {
                damage = Math.min(damage, maxDamage - 1);
            }
        }
        state.setDamage(damage);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (stack.getMaxDamage() <= 0) {
            return 0;
        }

        if (amount <= 0) {
            return 0;
        }

        var cap = stack.getCapability(BLADESTATE).orElseThrow(NullPointerException::new);
        boolean current = cap.isBroken();

        if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
            amount = 0;
            stack.setDamageValue(stack.getMaxDamage() - 1);
            cap.setBroken(!MinecraftForge.EVENT_BUS.post(new SlashBladeEvent.BreakEvent(stack, cap)));
        }

        if (current != cap.isBroken()) {
            onBroken.accept(entity);
            if (entity instanceof ServerPlayer player) {
                stack.getShareTag();
                CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            }

            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_BROKEN.get(stack.getItem()));
            }
        }

        if (cap.isBroken() && this.isDestructable(stack)) {
            stack.shrink(1);
        }

        return amount;
    }

    public static Consumer<LivingEntity> getOnBroken(ItemStack stack) {
        return (user) -> {
            user.broadcastBreakEvent(user.getUsedItemHand());

            var state = stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
            if (stack.isEnchanted()) {
                int count = state.getProudSoulCount() >= SlashBladeConfig.MAX_ENCHANTED_PROUDSOUL_DROP.get() * 100 ?
                        SlashBladeConfig.MAX_ENCHANTED_PROUDSOUL_DROP.get() : Math.max(1, state.getProudSoulCount() / 100);
                List<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValues().stream()
                        .filter(stack::canApplyAtEnchantingTable)
                        .filter(enchantment -> !SlashBladeConfig.NON_DROPPABLE_ENCHANTMENT.get()
                                .contains(Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantment)).toString()))
                        .toList();
                for (int i = 0; i < count; i += 1) {
                    ItemStack enchanted_soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                    Enchantment enchant = enchantments.get(user.getRandom().nextInt(0, enchantments.size()));
                    if (enchant != null) {
                        enchanted_soul.enchant(enchant, 1);
                        ItemEntity itemEntity = new ItemEntity(user.getLevel(), user.getX(), user.getY(), user.getZ(),
                                enchanted_soul);
                        itemEntity.setDefaultPickUpDelay();
                        user.getLevel().addFreshEntity(itemEntity);
                    }
                    state.setProudSoulCount(state.getProudSoulCount() - 100);
                }
            }
            ItemStack soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());

            int count = state.getProudSoulCount() >= SlashBladeConfig.MAX_PROUDSOUL_DROP.get() * 100 ?
                    SlashBladeConfig.MAX_PROUDSOUL_DROP.get() : Math.max(1, state.getProudSoulCount() / 100);

            soul.setCount(count);
            state.setProudSoulCount(state.getProudSoulCount() - (count * 100));

            ItemEntity itementity = new ItemEntity(user.getLevel(), user.getX(), user.getY(), user.getZ(), soul);
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, user.getLevel()) {
                static final String isReleased = "isReleased";

                @Override
                public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource ds) {

                    CompoundTag tag = this.getPersistentData();

                    if (!tag.getBoolean(isReleased)) {
                        this.getPersistentData().putBoolean(isReleased, true);

                        if (this.getLevel() instanceof ServerLevel) {
                            // 1.19.2中getOwner()返回UUID，需要修改获取thrower的逻辑
                            Entity thrower = null;

                            if (thrower != null) {
                                thrower.getPersistentData().remove(BREAK_ACTION_TIMEOUT);
                            }
                        }
                    }

                    return super.causeFallDamage(distance, damageMultiplier, ds);
                }
            };

            e.restoreFrom(itementity);
            e.init();
            e.push(0, 0.4, 0);

            e.setModel(state.getModel().orElse(DefaultResources.resourceDefaultModel));
            e.setTexture(state.getTexture().orElse(DefaultResources.resourceDefaultTexture));

            e.setPickUpDelay(20 * 2);
            e.setGlowingTag(true);

            e.setAirSupply(-1);

            e.setThrower(user.getUUID());

            user.getLevel().addFreshEntity(e);

            user.getPersistentData().putLong(BREAK_ACTION_TIMEOUT, user.getLevel().getGameTime() + 20 * 5);
        };
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {

        stack.getCapability(BLADESTATE).ifPresent((state) -> {
            ResourceLocation loc = state.resolvCurrentComboState(attacker);
            ComboState cs = ComboStateRegistry.REGISTRY.get().getValue(loc) != null
                    ? ComboStateRegistry.REGISTRY.get().getValue(loc)
                    : ComboStateRegistry.NONE.get();

            if (MinecraftForge.EVENT_BUS.post(new SlashBladeEvent.HitEvent(stack, state, target, attacker))) {
                return;
            }

            if (cs != null) {
                cs.hitEffect(target, attacker);
            }
            stack.hurtAndBreak(1, attacker, ItemSlashBlade.getOnBroken(stack));

        });

        return true;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level worldIn, BlockState state, @NotNull BlockPos pos,
                             @NotNull LivingEntity entityLiving) {

        if (state.getDestroySpeed(worldIn, pos) != 0.0F) {
            stack.getCapability(BLADESTATE).ifPresent((s) -> stack.hurtAndBreak(1, entityLiving, ItemSlashBlade.getOnBroken(stack)));
        }

        return true;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft) {
        int elapsed = this.getUseDuration(stack) - timeLeft;

        if (!worldIn.isClientSide()) {

            stack.getCapability(BLADESTATE).ifPresent((state) -> {

                var swordType = SwordType.from(stack);
                if (state.isBroken() || state.isSealed() || !(swordType.contains(SwordType.ENCHANTED))) {
                    return;
                }

                ResourceLocation sa = state.doChargeAction(entityLiving, elapsed);
                boolean isCreative = false;
                // sa.tickAction(entityLiving);
                if (!sa.equals(ComboStateRegistry.NONE.getId())) {
                    if (entityLiving instanceof Player player) {
                        isCreative = player.getAbilities().instabuild;
                    }
                    if (!isCreative) {
                        var cost = state.getSlashArts().getProudSoulCost();
                        if (state.getProudSoulCount() >= cost) {
                            state.setProudSoulCount(state.getProudSoulCount() - cost);
                        } else {
                            stack.hurtAndBreak(1, entityLiving, ItemSlashBlade.getOnBroken(stack));
                        }
                    }
                    entityLiving.swing(InteractionHand.MAIN_HAND);
                }
            });
        }
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity player, ItemStack stack, int count) {

        stack.getCapability(BLADESTATE).ifPresent((state) -> {

            (ComboStateRegistry.REGISTRY.get().getValue(state.getComboSeq()) != null
                    ? ComboStateRegistry.REGISTRY.get().getValue(state.getComboSeq())
                    : ComboStateRegistry.NONE.get()).holdAction(player);
            var swordType = SwordType.from(stack);
            if (state.isBroken() || state.isSealed() || !(swordType.contains(SwordType.ENCHANTED))) {
                return;
            }
            if (!player.getLevel().isClientSide()) {
                int ticks = player.getTicksUsingItem();
                int fullChargeTicks = state.getFullChargeTicks(player);
                if (0 < ticks) {
                    if (ticks == fullChargeTicks) {// state.getFullChargeTicks(player)){
                        Vec3 pos = player.getEyePosition(1.0f).add(player.getLookAngle());
                        ((ServerLevel) player.getLevel()).sendParticles(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 7, 0.7,
                                0.7, 0.7, 0.02);
                    }
                }
            }
        });
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        stack.getCapability(BLADESTATE).ifPresent((state) -> {
            if (MinecraftForge.EVENT_BUS
                    .post(new SlashBladeEvent.UpdateEvent(stack, state, worldIn, entityIn, itemSlot, isSelected))) {
                return;
            }

            if (entityIn instanceof Player player) {
                if (!SlashBladeConfig.SELF_REPAIR_ENABLE.get()) {
                    return;
                }
                if (!isSelected) {
                    var swordType = SwordType.from(stack);
                    boolean hasHunger = player.hasEffect(MobEffects.HUNGER) && SlashBladeConfig.HUNGER_CAN_REPAIR.get();
                    if (swordType.contains(SwordType.BEWITCHED) || hasHunger) {
                        if (stack.getDamageValue() > 0 && player.getFoodData().getFoodLevel() > 0) {
                            int hungerAmplifier = hasHunger ? player.getEffect(MobEffects.HUNGER).getAmplifier() : 0;
                            int level = 1 + hungerAmplifier;
                            Boolean expCostFlag = SlashBladeConfig.SELF_REPAIR_COST_EXP.get();
                            int expCost = SlashBladeConfig.BEWITCHED_EXP_COST.get() * level;

                            if (expCostFlag && player.experienceLevel < expCost) {
                                return;
                            }

                            player.giveExperiencePoints(expCostFlag ? -expCost : 0);
                            player.causeFoodExhaustion(
                                    SlashBladeConfig.BEWITCHED_HUNGER_EXHAUSTION.get().floatValue() * level);
                            stack.setDamageValue(stack.getDamageValue() - level);
                        }
                    }
                }
            }
            if (entityIn instanceof LivingEntity living) {
                entityIn.getCapability(INPUT_STATE).ifPresent(mInput -> mInput.getScheduler().onTick(living));

                /*
                 * if(0.5f > state.getDamage()) state.setDamage(0.99f);
                 */
                ResourceLocation loc = state.resolvCurrentComboState(living);
                ComboState cs = ComboStateRegistry.REGISTRY.get().getValue(loc) != null
                        ? ComboStateRegistry.REGISTRY.get().getValue(loc)
                        : ComboStateRegistry.NONE.get();

                if (isInMainhand(stack, isSelected, living)) {
                    if (cs != null) {
                        cs.tickAction(living);
                    } else if (!loc.equals(state.getComboRoot())) {
                        state.setComboSeq(state.getComboRoot());
                    }
                }
            }
        });
    }

    public static boolean isInMainhand(ItemStack stack, boolean isSelected, LivingEntity living) {
        return isSelected && stack.equals(living.getMainHandItem(), false);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        stack.getCapability(BLADESTATE).ifPresent(state -> {
            if (!state.isEmpty()) {
                tag.put("bladeState", state.serializeNBT());
            }
        });
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            if (nbt.contains("bladeState")) {
                stack.getCapability(BLADESTATE).ifPresent(state -> state.deserializeNBT(nbt.getCompound("bladeState")));
            }
        }
        super.readShareTag(stack, nbt);
    }

    // damage ----------------------------------------------------------

    @Override
    public int getDamage(ItemStack stack) {
        return stack.getCapability(BLADESTATE).filter(s -> !s.isEmpty()).map(ISlashBladeState::getDamage).orElse(0);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return stack.getCapability(BLADESTATE).filter(s -> !s.isEmpty()).map(ISlashBladeState::getMaxDamage).orElse(this.getTier().getUses());
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        return stack.getCapability(BLADESTATE).filter((s) -> !s.getTranslationKey().isBlank())
                .map(ISlashBladeState::getTranslationKey).orElseGet(() -> stackDefaultDescriptionId(stack));
    }

    public ResourceLocation getBladeId(ItemStack stack) {
        return stack.getCapability(BLADESTATE).filter((s) -> !s.getTranslationKey().isBlank())
                .map((state) -> parseBladeID(state.getTranslationKey())).orElseGet(() -> stackDefaultId(stack));
    }

    private String stackDefaultDescriptionId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return super.getDescriptionId(stack);
        }
        String key = tag.getCompound("bladeState").getString("translationKey");
        return !key.isBlank() ? key : super.getDescriptionId(stack);
    }

    private ResourceLocation stackDefaultId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return ForgeRegistries.ITEMS.getKey(this);
        }
        String key = tag.getCompound("bladeState").getString("translationKey");
        return !key.isBlank() ? parseBladeID(key) : ForgeRegistries.ITEMS.getKey(this);
    }

    public static ResourceLocation parseBladeID(String key) {
        return ResourceLocation.tryParse(key.substring(5).replaceFirst("\\.", ":"));
    }

    public boolean isDestructable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {

        if (Ingredient.of(ItemTags.STONE_TOOL_MATERIALS).test(repair)) {
            return true;
        }

        /*
         * Tag<Item> tags = ItemTags.getCollection().get(new
         * ResourceLocation("slashblade","proudsouls"));
         *
         * if(tags != null){ boolean result = Ingredient.fromTag(tags).test(repair); }
         */

        // todo: repair custom material
        if (repair.is(SlashBladeItemTags.PROUD_SOULS)) {
            return true;
        }
        return super.isValidRepairItem(toRepair, repair);
    }

    RangeMap<Comparable<?>, Object> refineColor = ImmutableRangeMap.builder()
            .put(Range.lessThan(10), ChatFormatting.GRAY).put(Range.closedOpen(10, 50), ChatFormatting.YELLOW)
            .put(Range.closedOpen(50, 100), ChatFormatting.GREEN).put(Range.closedOpen(100, 150), ChatFormatting.AQUA)
            .put(Range.closedOpen(150, 200), ChatFormatting.BLUE).put(Range.atLeast(200), ChatFormatting.LIGHT_PURPLE)
            .build();

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
            this.appendSwordType(stack, worldIn, tooltip, flagIn); // �?
            this.appendProudSoulCount(tooltip, stack);
            this.appendKillCount(tooltip, stack);
            this.appendSlashArt(stack, tooltip, s); // �?
            this.appendRefineCount(tooltip, stack);
            this.appendSpecialEffects(tooltip, s); // �?
        });

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendSlashArt(ItemStack stack, List<Component> tooltip, @NotNull ISlashBladeState s) {
        var swordType = SwordType.from(stack);
        if (swordType.contains(SwordType.BEWITCHED) && !swordType.contains(SwordType.SEALED)) {
            tooltip.add(Component.translatable("slashblade.tooltip.slash_art", s.getSlashArts().getDescription())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendRefineCount(List<Component> tooltip, @NotNull ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return;
        }
        int refine = tag.getCompound("bladeState").getInt("RepairCounter");
        if (refine > 0) {
            tooltip.add(Component.translatable("slashblade.tooltip.refine", refine)
                    .withStyle((ChatFormatting) refineColor.get(refine)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendProudSoulCount(List<Component> tooltip, @NotNull ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return;
        }
        int proudsoul = tag.getCompound("bladeState").getInt("proudSoul");
        if (proudsoul > 0) {
            MutableComponent countComponent = Component.translatable("slashblade.tooltip.proud_soul", proudsoul)
                    .withStyle(ChatFormatting.GRAY);
            if (proudsoul > 10000) {
                countComponent = countComponent.withStyle(ChatFormatting.DARK_PURPLE);
            }
            tooltip.add(countComponent);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendKillCount(List<Component> tooltip, @NotNull ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return;
        }
        int killCount = tag.getCompound("bladeState").getInt("killCount");
        if (killCount > 0) {
            MutableComponent killCountComponent = Component.translatable("slashblade.tooltip.killcount", killCount)
                    .withStyle(ChatFormatting.GRAY);
            if (killCount > 1000) {
                killCountComponent = killCountComponent.withStyle(ChatFormatting.DARK_PURPLE);
            }
            tooltip.add(killCountComponent);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendSpecialEffects(List<Component> tooltip, @NotNull ISlashBladeState s) {
        if (s.getSpecialEffects().isEmpty()) {
            return;
        }

        Minecraft mcinstance = Minecraft.getInstance();
        Player player = mcinstance.player;

        s.getSpecialEffects().forEach(se -> {

            boolean showingLevel = SpecialEffect.getRequestLevel(se) > 0;

            if (player != null) {
                tooltip.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
                                Component.literal(showingLevel ? String.valueOf(SpecialEffect.getRequestLevel(se)) : "")
                                        .withStyle(SpecialEffect.isEffective(se, player.experienceLevel) ? ChatFormatting.RED
                                                : ChatFormatting.DARK_GRAY))
                        .withStyle(ChatFormatting.GRAY));
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void appendSwordType(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        var swordType = SwordType.from(stack);
        boolean goldenFlag = swordType.containsAll(List.of(SwordType.SOULEATER, SwordType.FIERCEREDGE));
        if (swordType.contains(SwordType.SEALED)) {
            return;
        }
        if (swordType.contains(SwordType.BEWITCHED)) {
            tooltip.add(
                    Component.translatable("slashblade.sword_type.bewitched")
                            .withStyle(goldenFlag ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE));
        } else if (swordType.contains(SwordType.ENCHANTED)) {
            tooltip.add(Component.translatable("slashblade.sword_type.enchanted").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("slashblade.sword_type.noname").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemSlashBlade) {
            return new NamedBladeStateCapabilityProvider(stack);
        }
        return null;
    }

    /**
     * @return true = cancel : false = swing
     */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return stack.getCapability(BLADESTATE).filter(s -> s.getLastActionTime() == entity.getLevel().getGameTime())
                .isEmpty();
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    /**
     * 原来的方法替换掉落实体时无法Copy假物品实体相关的NBT，因为获取物品指令是先生成的物品实体再设置的假物�?
     */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!(entity instanceof BladeItemEntity)) {
            Level world = entity.getLevel();
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, world);
            e.restoreFrom(entity);
            e.init();
            entity.discard();
            world.addFreshEntity(e);
        }
        return false;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level world) {
        return super.getEntityLifespan(itemStack, world);// Short.MAX_VALUE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {

        consumer.accept(new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer renderer = new SlashBladeTEISR(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });

        super.initializeClient(consumer);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        // 只在我们的创造模式标签中添加物品变体
        if (group == SlashBladeCreativeGroup.TAB) {
            // 添加默认的无铭刀
            super.fillItemCategory(group, items);
            
            // 添加所有命名刀变体
            addNamedBladesToCreative(items);
        }
    }

    private void addNamedBladesToCreative(NonNullList<ItemStack> items) {
        try {
            // 添加鄂门
            ItemStack orotiagito = new ItemStack(this);
            orotiagito.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/orotiagito.png"));
                s.setBaseAttackModifier(7.0F);
                s.setMaxDamage(60);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "wave_edge"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.orotiagito");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = orotiagito.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            orotiagito.setHoverName(Component.translatable("item.slashblade.orotiagito"));
            items.add(orotiagito);
            
            // 添加村正
            ItemStack muramasa = new ItemStack(this);
            muramasa.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/muramasa.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/muramasa.png"));
                s.setBaseAttackModifier(7.0F);
                s.setMaxDamage(50);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "drive_vertical"));
                s.setTranslationKey("item.slashblade.muramasa");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = muramasa.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            muramasa.setHoverName(Component.translatable("item.slashblade.muramasa"));
            items.add(muramasa);
            
            // 添加阎魔刀
            ItemStack yamato = new ItemStack(this);
            yamato.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
                s.setBaseAttackModifier(7.0F);
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.yamato");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yamato.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            yamato.setHoverName(Component.translatable("item.slashblade.yamato"));
            items.add(yamato);
            
            // 添加锈刀
            ItemStack agitoRust = new ItemStack(this);
            agitoRust.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/agito_rust.png"));
                s.setBaseAttackModifier(3.0F);
                s.setMaxDamage(60);
                s.setSealed(true);
                s.setTranslationKey("item.slashblade.sabigatana");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = agitoRust.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            agitoRust.setHoverName(Component.translatable("item.slashblade.sabigatana"));
            items.add(agitoRust);
            
            // 添加付丧结月
            ItemStack tukumo = new ItemStack(this);
            tukumo.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/a_tukumo.png"));
                s.setBaseAttackModifier(6.0F);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "drive_horizontal"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.yuzukitukumo");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = tukumo.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            tukumo.setHoverName(Component.translatable("item.slashblade.yuzukitukumo"));
            items.add(tukumo);
            
            // 添加枯石大刀
            ItemStack koseki = new ItemStack(this);
            koseki.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/dios/dios.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/dios/koseki.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "drive_vertical"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "wither_edge"));
                s.setTranslationKey("item.slashblade.koseki");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = koseki.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            koseki.setHoverName(Component.translatable("item.slashblade.koseki"));
            items.add(koseki);
            
            // 添加散华
            ItemStack sange = new ItemStack(this);
            sange.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.png"));
                s.setBaseAttackModifier(6.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "void_slash"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.sange");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = sange.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            sange.setHoverName(Component.translatable("item.slashblade.sange"));
            items.add(sange);
            
            // 添加钢剑胴田贯
            ItemStack doutanuki = new ItemStack(this);
            doutanuki.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/muramasa.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/doutanuki.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(60);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "circle_slash"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.doutanuki");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = doutanuki.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            doutanuki.setHoverName(Component.translatable("item.slashblade.doutanuki"));
            items.add(doutanuki);
            
            // 添加夜叉
            ItemStack yasha = new ItemStack(this);
            yasha.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha.png"));
                s.setBaseAttackModifier(6.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "sakura_end"));
                s.setTranslationKey("item.slashblade.yasha");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yasha.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            yasha.setHoverName(Component.translatable("item.slashblade.yasha"));
            items.add(yasha);
            
            // 添加真夜叉
            ItemStack yashaTrue = new ItemStack(this);
            yashaTrue.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha_true.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha.png"));
                s.setBaseAttackModifier(6.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "circle_slash"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.yasha_true");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yashaTrue.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            yashaTrue.setHoverName(Component.translatable("item.slashblade.yasha_true"));
            items.add(yashaTrue);
            
            // 添加黑狐
            ItemStack foxBlack = new ItemStack(this);
            foxBlack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/sange/black.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "piercing"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.fox_black");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = foxBlack.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            foxBlack.setHoverName(Component.translatable("item.slashblade.fox_black"));
            items.add(foxBlack);
            
            // 添加白狐
            ItemStack foxWhite = new ItemStack(this);
            foxWhite.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/sange/white.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(70);
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("item.slashblade.fox_white");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = foxWhite.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            foxWhite.setHoverName(Component.translatable("item.slashblade.fox_white"));
            items.add(foxWhite);
            
            // 添加鄂门（封）
            ItemStack orotiagitoSealed = new ItemStack(this);
            orotiagitoSealed.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/agito_true.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(60);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "wave_edge"));
                s.setSealed(true);
                s.setTranslationKey("item.slashblade.orotiagito_sealed");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = orotiagitoSealed.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            orotiagitoSealed.setHoverName(Component.translatable("item.slashblade.orotiagito_sealed"));
            items.add(orotiagitoSealed);
            
            // 添加鄂门（锈）
            ItemStack orotiagitoRust = new ItemStack(this);
            orotiagitoRust.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/agito_rust_true.png"));
                s.setBaseAttackModifier(3.0F);
                s.setMaxDamage(60);
                s.setSealed(true);
                s.setTranslationKey("item.slashblade.orotiagito_rust");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = orotiagitoRust.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            orotiagitoRust.setHoverName(Component.translatable("item.slashblade.orotiagito_rust"));
            items.add(orotiagitoRust);
            
            // 添加断阎魔刀
            ItemStack yamatoBroken = new ItemStack(this);
            yamatoBroken.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
                s.setBaseAttackModifier(7.0F);
                s.setBroken(true);
                s.setSealed(true);
                s.setTranslationKey("item.slashblade.yamato_broken");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yamatoBroken.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            yamatoBroken.setHoverName(Component.translatable("item.slashblade.yamato_broken"));
            items.add(yamatoBroken);
        } catch (Exception e) {
            SlashBlade.LOGGER.warn("Failed to add named blades to creative menu: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 处理铁砧重命名逻辑
    public static ItemStack applyAnvilRename(ItemStack stack, String newName) {
        ItemStack result = stack.copy();
        result.setHoverName(Component.literal(newName));
        
        // 当重命名时，将刀变为"印"刀
        result.getCapability(BLADESTATE).ifPresent(state -> {
            // 检查是否是无铭或由无铭合成的刀
            ItemSlashBlade bladeItem = (ItemSlashBlade) stack.getItem();
            ResourceLocation bladeId = bladeItem.getBladeId(stack);
            if (bladeId != null && bladeId.getPath().equals("slashblade")) {
                // 移除"封"状态，添加"印"状态
                state.setSealed(false);
                // 这里可以添加印刀的特殊属性或标签
                // 例如：state.setSpecialEffect("seal");
            }
        });
        
        return result;
    }
} 

// 添加铁砧事件处理器
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = SlashBlade.MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
class AnvilEventHandler {
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        String newName = event.getName();
        
        if (!left.isEmpty() && left.getItem() instanceof ItemSlashBlade && newName != null && !newName.isEmpty()) {
            // 处理铁砧重命名
            ItemStack result = ItemSlashBlade.applyAnvilRename(left, newName);
            event.setOutput(result);
            event.setCost(1); // 设置重命名的经验成本
        }
        
        // 处理封刀附魔逻辑
        if (!left.isEmpty() && left.getItem() instanceof ItemSlashBlade && !event.getRight().isEmpty() && event.getRight().getItem() instanceof EnchantedBookItem) {
            ItemStack result = left.copy();
            
            // 应用附魔
            // 在1.19.2中，使用EnchantmentHelper来获取和应用附魔
            net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(
                net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(event.getRight()),
                result
            );
            
            // 封刀后变为妖刀
            result.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
                state.setSealed(true);
            });
            
            event.setOutput(result);
            event.setMaterialCost(1);
            event.setCost(5); // 设置附魔的经验成本
        }
    }
}






