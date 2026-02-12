package mods.flammpfeil.slashblade.client.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SlashBladeBookScreen extends Screen {
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("textures/gui/book.png");
    private static final int BOOK_WIDTH = 192;
    private static final int BOOK_HEIGHT = 192;
    private static final int ICON_SIZE = 16;
    private static final int ICON_SPACING = 24;
    
    private int leftPos;
    private int topPos;
    private int currentPage = 0;
    private List<SlashBladeDefinition> allBlades = new ArrayList<>();
    private SlashBladeDefinition selectedBlade = null;
    private boolean isDetailView = false;
    
    public SlashBladeBookScreen() {
        super(Component.literal("启程"));
        // 初始化空列表
        allBlades = new ArrayList<>();
    }
    
    @Override
    protected void init() {
        super.init();
        leftPos = (width - BOOK_WIDTH) / 2;
        topPos = (height - BOOK_HEIGHT) / 2;
        
        // 只有在第一次初始化或allBlades为空时才重新获取拔刀剑列表
        if (allBlades.isEmpty()) {
            // 尝试获取所有注册的拔刀剑定义
            Registry<SlashBladeDefinition> registry = null;
            
            // 方法1：尝试通过Minecraft获取（更可靠的方式）
            try {
                var minecraft = Minecraft.getInstance();
                var level = minecraft.level;
                if (level != null) {
                    registry = SlashBlade.getSlashBladeDefinitionRegistry(level);
                    SlashBlade.LOGGER.info("Got registry via level: {}", registry != null ? "success" : "failed");
                } else {
                    SlashBlade.LOGGER.warn("Level is null in init()");
                }
            } catch (Exception e) {
                SlashBlade.LOGGER.warn("Failed to get registry via level: {}", e.getMessage(), e);
            }
            
            // 方法2：尝试通过BladeModelManager获取
            if (registry == null) {
                registry = BladeModelManager.getClientSlashBladeRegistry();
                SlashBlade.LOGGER.info("Got registry via BladeModelManager: {}", registry != null ? "success" : "failed");
            }
            
            if (registry != null) {
                // 遍历注册表，添加所有拔刀剑定义
                for (SlashBladeDefinition definition : registry) {
                    allBlades.add(definition);
                }
                SlashBlade.LOGGER.info("Found {} blades from registry", allBlades.size());
            } else {
                SlashBlade.LOGGER.warn("Failed to get slash blade registry, trying fallback methods");
                // 直接从SlashBladeBuiltInRegistry获取所有内置拔刀剑
                addAllBuiltinBlades();
                SlashBlade.LOGGER.info("After fallback, found {} blades", allBlades.size());
            }
        }
        
        // 清除之前的按钮
        this.renderables.clear();
        this.children().clear();
        
        // 添加页面导航按钮（仅在列表视图显示）
        if (!isDetailView) {
            // 添加下一页按钮
            addRenderableWidget(new Button(leftPos + BOOK_WIDTH - 50, topPos + BOOK_HEIGHT - 30, 40, 20, Component.literal("下一页"), 
                    button -> currentPage++));
            
            // 添加上一页按钮
            addRenderableWidget(new Button(leftPos + 10, topPos + BOOK_HEIGHT - 30, 40, 20, Component.literal("上一页"), 
                    button -> currentPage = Math.max(0, currentPage - 1)));
        }
        
        // 添加关闭按钮
        addRenderableWidget(new Button(leftPos + BOOK_WIDTH / 2 - 20, topPos + BOOK_HEIGHT - 30, 40, 20, Component.literal("关闭"), 
                button -> this.onClose()));
        
        // 添加返回按钮（仅在详情页显示）
        if (isDetailView) {
            addRenderableWidget(new Button(leftPos + 10, topPos + 10, 40, 20, Component.literal("返回"), 
                    button -> {
                        isDetailView = false;
                        selectedBlade = null;
                        this.init(); // 重新初始化按钮
                    }));
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        
        // 渲染书的背景
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        
        // 渲染标题
        drawCenteredString(poseStack, font, title, leftPos + BOOK_WIDTH / 2, topPos + 10, 0xFFFFFF);
        
        if (isDetailView && selectedBlade != null) {
            renderDetailView(poseStack, mouseX, mouseY, partialTick);
        } else {
            renderIconListView(poseStack, mouseX, mouseY, partialTick);
        }
        
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
    
    private void renderIconListView(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // 渲染拔刀剑图标列表
        int startIndex = currentPage * 9;
        int endIndex = Math.min(startIndex + 9, allBlades.size());
        
        // 计算图标区域的尺寸和居中位置
        int gridWidth = 3 * ICON_SPACING;
        int gridHeight = 3 * ICON_SPACING;
        int gridX = leftPos + (BOOK_WIDTH - gridWidth) / 2;
        int gridY = topPos + (BOOK_HEIGHT - gridHeight) / 2 - 10; // 向上微调10像素
        
        for (int i = startIndex; i < endIndex; i++) {
            SlashBladeDefinition bladeDef = allBlades.get(i);
            ItemStack bladeStack = bladeDef.getBlade();
            
            int row = (i - startIndex) / 3;
            int col = (i - startIndex) % 3;
            // 计算每个图标的居中位置
            int x = gridX + col * ICON_SPACING;
            int y = gridY + row * ICON_SPACING;
            
            // 渲染物品图标
            itemRenderer.renderGuiItem(bladeStack, x, y);
            
            // 检查鼠标是否悬停在图标上
            if (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE) {
                // 渲染物品名称作为简单提示
                renderTooltip(poseStack, bladeStack.getHoverName(), mouseX, mouseY);
            }
        }
    }
    
    private void renderDetailView(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (selectedBlade == null) return;
        
        ItemStack bladeStack = selectedBlade.getBlade();
        String bladeName = bladeStack.getHoverName().getString();
        
        // 渲染拔刀剑名称，居中显示
        drawCenteredString(poseStack, font, bladeStack.getHoverName(), leftPos + BOOK_WIDTH / 2, topPos + 20, 0xFFFFFF);
        
        // 获取拔刀剑的获取方法或合成信息
        String bladeId = selectedBlade.getName().getPath();
        String acquisitionMethod = getAcquisitionMethod(bladeId);
        
        // 检查是否可以合成
        boolean canCraft = canCraft(bladeId);
        
        if (canCraft) {
            // 可以合成，显示合成配方
            drawCenteredString(poseStack, font, "合成配方", leftPos + BOOK_WIDTH / 2, topPos + 50, 0xFFFFFF);
            
            // 渲染九宫格合成槽
            renderCraftingRecipe(poseStack, mouseX, mouseY, bladeId, bladeStack);
        } else {
            // 不可合成，显示获取方法
            drawCenteredString(poseStack, font, "获取方法", leftPos + BOOK_WIDTH / 2, topPos + 50, 0xFFFFFF);
            drawCenteredString(poseStack, font, acquisitionMethod, leftPos + BOOK_WIDTH / 2, topPos + 70, 0xFFFFFF);
        }
    }
    
    /**
     * 检查拔刀剑是否可以合成
     */
    private boolean canCraft(String bladeId) {
        // 根据重锋合成表.txt，以下拔刀剑有真实的合成配方
        // 其他拔刀剑没有合成表，需要显示获取方法
        String[] craftableBlades = {
            "rodai_wooden", "rodai_stone", "rodai_iron", "rodai_golden", "rodai_diamond", "rodai_netherite",
            "slashblade_wood", "slashblade_bamboo", "slashblade_silverbamboo", "slashblade_white", "slashblade",
            "ruby", "fox_black", "fox_white", "muramasa", "tagayasan", "agito", "orotiagito_sealed", "orotiagito",
            "doutanuki", "sabigatana", "yuzukitukumo", "yamato", "rusty_sword"
        };
        
        for (String craftable : craftableBlades) {
            if (bladeId.equals(craftable)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取拔刀剑的获取方法
     */
    private String getAcquisitionMethod(String bladeId) {
        switch (bladeId) {
            case "koseki":
                return "让凋零的爆炸炸到刀架台上的太刀";
            case "sange":
                return "击败凋零有概率掉落";
            case "rusty_sword":
                return "僵尸有概率携带";
            case "agito_rust":
            case "orotiagito_rust":
            case "sabigatana_broken":
            case "yamato_broken":
                return "通过破坏对应的拔刀剑获得";
            case "yasha":
            case "yasha_true":
                return "通过特殊事件获得";
            default:
                return "通过游戏内事件或探索获得";
        }
    }
    
    /**
     * 获取合成配方备注
     */
    private String getCraftingRecipeNote(String bladeId) {
        switch (bladeId) {
            case "slashblade_wood":
                return "所有类型木头均可以使用";
            case "slashblade_bamboo":
                return "";
            case "slashblade_silverbamboo":
                return "";
            case "slashblade_white":
                return "";
            case "slashblade":
                return "白鞘必须是用完耐久断裂的";
            case "agito":
            case "orotiagito_sealed":
                return "";
            case "doutanuki":
                return "锈刀需要：耀魂数1000、杀敌数100、精炼次数10";
            case "fox_white":
                return "利刀「无名」红玉需至少携带附魔：亡灵杀手Ⅰ";
            case "fox_black":
                return "利刀「无名」红玉需至少携带附魔：抢夺Ⅰ";
            case "muramasa":
                return "无铭「无名」需要：耀魂数10000、精炼次数20";
            case "orotiagito":
                return "「鄂门」需要：耀魂数1000、杀敌数1000、精炼次数10";
            case "tagayasan":
                return "无铭刀「木偶」需要：耀魂数1000、精炼次数10并且至少携带附魔：耐久Ⅰ";
            case "yamato":
                return "合成材料中的魔剑「阎魔刀」为断裂的（耐久为0）的";
            case "yuzukitukumo":
                return "无铭「无名」需至少携带附魔：火焰附加Ⅰ";
            case "ruby":
                return "名刀「银纸竹光」必须是用完耐久断裂的";
            case "rusty_sword":
                return "为断裂的锈刀恢复耐久";
            case "rodai_wooden":
            case "rodai_stone":
            case "rodai_iron":
            case "rodai_golden":
            case "rodai_diamond":
            case "rodai_netherite":
                return "所有类型木头均可以使用";
            default:
                return "";
        }
    }
    
    /**
     * 渲染合成配方
     */
    private void renderCraftingRecipe(PoseStack poseStack, int mouseX, int mouseY, String bladeId, ItemStack resultStack) {
        // 渲染九宫格合成槽
        int recipeStartX = leftPos + (BOOK_WIDTH - 60) / 2; // 居中显示九宫格
        int recipeStartY = topPos + 70;
        
        // 获取合成材料
        ItemStack[][] recipe = getCraftingRecipe(bladeId);
        
        // 渲染九宫格背景和物品
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = recipeStartX + col * 20;
                int y = recipeStartY + row * 20;
                // 渲染空槽背景
                blit(poseStack, x, y, 0, 16, 16, 16, 256, 256);
                
                // 渲染物品
                ItemStack itemStack = recipe[row][col];
                if (!itemStack.isEmpty()) {
                    itemRenderer.renderGuiItem(itemStack, x, y);
                }
            }
        }
        
        // 在九宫格下方显示合成结果
        drawString(poseStack, font, "=", recipeStartX + 30, recipeStartY + 65, 0xFFFFFF);
        int resultX = recipeStartX + 40;
        int resultY = recipeStartY + 60;
        blit(poseStack, resultX, resultY, 0, 16, 16, 16, 256, 256);
        itemRenderer.renderGuiItem(resultStack, resultX, resultY);
        
        // 渲染物品提示
        renderItemTooltips(poseStack, mouseX, mouseY, recipeStartX, recipeStartY, recipe, resultStack);
        
        // 显示合成备注
        String note = getCraftingRecipeNote(bladeId);
        if (!note.isEmpty()) {
            int noteX = leftPos + (BOOK_WIDTH - font.width(note)) / 2;
            int noteY = topPos + BOOK_HEIGHT - 40;
            drawString(poseStack, font, note, noteX, noteY, 0xAAAAAA);
        }
    }
    
    /**
     * 获取合成配方
     */
    private ItemStack[][] getCraftingRecipe(String bladeId) {
        // 创建一个3x3的空配方
        ItemStack[][] recipe = new ItemStack[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                recipe[i][j] = ItemStack.EMPTY;
            }
        }
        
        // 直接使用Items类的静态引用
        
        // 根据重锋合成表.txt设置真实的合成配方
        switch (bladeId) {
            case "slashblade_wood":
                // 无铭刀「木偶」：[null,null,木头],[null,木头,null],[木剑,null,null] 注：所有类型木头均可以
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.OAK_LOG);
                recipe[1][1] = new ItemStack(net.minecraft.world.item.Items.OAK_LOG);
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.WOODEN_SWORD);
                break;
            case "slashblade_bamboo":
                // 无铭刀「竹光」：[null,null,竹子],[null,竹子,null],[无铭刀「木偶」,null,null]
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.BAMBOO);
                recipe[1][1] = new ItemStack(net.minecraft.world.item.Items.BAMBOO);
                recipe[2][0] = safeGetItemStack(SlashBlade.prefix("slashblade_wood"));
                break;
            case "slashblade_silverbamboo":
                // 名刀「银纸竹光」：[null,鸡蛋,铁锭],[线,无铭刀「竹光」,黑色染料],[纸,线,null]
                recipe[0][1] = new ItemStack(net.minecraft.world.item.Items.EGG);
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
                recipe[1][0] = new ItemStack(net.minecraft.world.item.Items.STRING);
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("slashblade_bamboo"));
                recipe[1][2] = new ItemStack(net.minecraft.world.item.Items.BLACK_DYE);
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.PAPER);
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.STRING);
                break;
            case "slashblade_white":
                // 利刃「白鞘」：[null,null,耀魂铁锭],[null,耀魂铁锭,null],[木偶,金锭,null]
                recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_ingot"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_ingot"));
                recipe[2][0] = safeGetItemStack(SlashBlade.prefix("slashblade_wood"));
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
                break;
            case "slashblade":
                // 无铭「无名」：[null,烈焰棒,金锭],[蓝色燃料,利刃「白鞘」,煤炭块],[线,金锭,null] 注：白鞘必须是用完耐久断裂的
                recipe[0][1] = new ItemStack(net.minecraft.world.item.Items.BLAZE_ROD);
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
                recipe[1][0] = new ItemStack(net.minecraft.world.item.Items.BLUE_DYE);
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("slashblade_white"));
                recipe[1][2] = new ItemStack(net.minecraft.world.item.Items.COAL_BLOCK);
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.STRING);
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
                break;
            case "agito":
                // 伪物「鄂门」：[null,耀魂碎片,null],[耀魂碎片,锈掉的鄂门,耀魂碎片],[null,耀魂碎片,null]
                recipe[0][1] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[1][0] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("agito_rust"));
                recipe[1][2] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[2][1] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                break;
            case "doutanuki":
                // 刚剑「胴田贯」：[null,null,耀魂宝珠],[null,锈刀,null],[耀魂宝珠,null,null] 注：锈刀需要：耀魂数1000、杀敌数100、精炼次数10
                recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("rusty_sword"));
                recipe[2][0] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                break;
            case "fox_white":
                // 狐月刀「白狐」：[null,黑曜石,羽毛],[烈焰粉,利刀「无名」红玉,耀魂结晶],[小麦,石英块,null] 注：利刀「无名」红玉需至少携带附魔：亡灵杀手Ⅰ
                recipe[0][1] = new ItemStack(net.minecraft.world.item.Items.OBSIDIAN);
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.FEATHER);
                recipe[1][0] = new ItemStack(net.minecraft.world.item.Items.BLAZE_POWDER);
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("ruby"));
                recipe[1][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_crystal"));
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.WHEAT);
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.QUARTZ_BLOCK);
                break;
            case "fox_black":
                // 狐月刀「黑狐」：[null,黑曜石,羽毛],[烈焰粉,利刀「无名」红玉,耀魂结晶],[小麦,石英块,null] 注：利刀「无名」红玉需至少携带附魔：抢夺Ⅰ
                recipe[0][1] = new ItemStack(net.minecraft.world.item.Items.OBSIDIAN);
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.FEATHER);
                recipe[1][0] = new ItemStack(net.minecraft.world.item.Items.BLAZE_POWDER);
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("ruby"));
                recipe[1][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_crystal"));
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.WHEAT);
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.QUARTZ_BLOCK);
                break;
            case "muramasa":
                // 「千鹤」村正：[耀魂宝珠,耀魂宝珠,耀魂宝珠],[耀魂宝珠,无铭「无名」,耀魂宝珠],[耀魂宝珠,耀魂宝珠,耀魂宝珠] 注：无铭「无名」需要：耀魂数10000、精炼次数20
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            recipe[i][j] = safeGetItemStack(SlashBlade.prefix("slashblade"));
                        } else {
                            recipe[i][j] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                        }
                    }
                }
                break;
            case "orotiagito_sealed":
                // 「鄂门」：[null,耀魂碎片,null],[耀魂碎片,锈掉的鄂门,耀魂碎片],[null,耀魂碎片,null]
                recipe[0][1] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[1][0] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("orotiagito_rust"));
                recipe[1][2] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[2][1] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                break;
            case "orotiagito":
                // 名刀「大蛇鄂门」：[耀魂碎片,耀魂宝珠,耀魂碎片],[耀魂宝珠,「鄂门」,耀魂宝珠],[耀魂碎片,耀魂宝珠,耀魂碎片] 注：「鄂门」需要：耀魂数1000、杀敌数1000、精炼次数10
                recipe[0][0] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[0][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[1][0] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("orotiagito_sealed"));
                recipe[1][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[2][0] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[2][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[2][2] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                break;
            case "ruby":
                // 利刀「无名」红玉：[红色燃料,耀魂铁锭,耀魂碎片],[耀魂铁锭,名刀「银纸竹光」,null],[线,null,null] 注：名刀「银纸竹光」必须是用完耐久断裂的
                recipe[0][0] = new ItemStack(net.minecraft.world.item.Items.RED_DYE);
                recipe[0][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_ingot"));
                recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul"));
                recipe[1][0] = safeGetItemStack(SlashBlade.prefix("proudsoul_ingot"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("slashblade_silverbamboo"));
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.STRING);
                break;
            case "rusty_sword":
                // 锈刀：[null,null,耀魂铁锭],[null,耀魂铁锭,null],[锈刀,null,null] 注：为断裂的锈刀恢复耐久
                recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_ingot"));
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_ingot"));
                recipe[2][0] = safeGetItemStack(SlashBlade.prefix("rusty_sword"));
                break;
            case "tagayasan":
                // 木刀「铁刀木」：[耀魂宝珠,末影之眼,耀魂宝珠],[末影珍珠,无铭刀「木偶」,末影珍珠],[耀魂宝珠,末影之眼,耀魂宝珠] 注：无铭刀「木偶」需要：耀魂数1000、精炼次数10并且至少携带附魔：耐久Ⅰ
                recipe[0][0] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[0][1] = new ItemStack(net.minecraft.world.item.Items.ENDER_EYE);
                recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[1][0] = new ItemStack(net.minecraft.world.item.Items.ENDER_PEARL);
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("slashblade_wood"));
                recipe[1][2] = new ItemStack(net.minecraft.world.item.Items.ENDER_PEARL);
                recipe[2][0] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.ENDER_EYE);
                recipe[2][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                break;
            case "yamato":
                // 魔剑「阎魔刀」：[耀魂宝珠,耀魂宝珠,耀魂宝珠],[耀魂宝珠,魔剑「阎魔刀」,耀魂宝珠],[耀魂宝珠,耀魂宝珠,耀魂宝珠] 注：合成材料中的魔剑「阎魔刀」为断裂的（耐久为0）的
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            recipe[i][j] = safeGetItemStack(SlashBlade.prefix("yamato_broken"));
                        } else {
                            recipe[i][j] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                        }
                    }
                }
                break;
            case "yuzukitukumo":
                // 宝刀「付丧」结月：[绿宝石块,耀魂宝珠,钻石块],[红石块,无铭「无名」,青金石块],[铁块,耀魂宝珠,金块] 注：无铭「无名」需至少携带附魔：火焰附加Ⅰ
                recipe[0][0] = new ItemStack(net.minecraft.world.item.Items.EMERALD_BLOCK);
                recipe[0][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[0][2] = new ItemStack(net.minecraft.world.item.Items.DIAMOND_BLOCK);
                recipe[1][0] = new ItemStack(net.minecraft.world.item.Items.REDSTONE_BLOCK);
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("slashblade"));
                recipe[1][2] = new ItemStack(net.minecraft.world.item.Items.LAPIS_BLOCK);
                recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.IRON_BLOCK);
                recipe[2][1] = safeGetItemStack(SlashBlade.prefix("proudsoul_sphere"));
                recipe[2][2] = new ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK);
                break;
            case "rodai_wooden":
            case "rodai_stone":
            case "rodai_iron":
            case "rodai_golden":
            case "rodai_diamond":
            case "rodai_netherite":
                // 鲁钝系列：对应的剑 + 银竹 + 线 + 骄傲之魂水晶/八面体
                if (bladeId.equals("rodai_diamond") || bladeId.equals("rodai_netherite")) {
                    recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_trapezohedron"));
                } else {
                    recipe[0][2] = safeGetItemStack(SlashBlade.prefix("proudsoul_crystal"));
                }
                recipe[1][1] = safeGetItemStack(SlashBlade.prefix("slashblade_silverbamboo"));
                
                if (bladeId.contains("wooden")) {
                    recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.WOODEN_SWORD);
                } else if (bladeId.contains("stone")) {
                    recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.STONE_SWORD);
                } else if (bladeId.contains("iron")) {
                    recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
                } else if (bladeId.contains("golden")) {
                    recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.GOLDEN_SWORD);
                } else if (bladeId.contains("diamond")) {
                    recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
                } else {
                    recipe[2][0] = new ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
                }
                recipe[2][1] = new ItemStack(net.minecraft.world.item.Items.STRING);
                break;
            default:
                // 其他拔刀剑暂时返回空配方
                break;
        }
        
        return recipe;
    }
    
    /**
     * 渲染物品提示
     */
    private void renderItemTooltips(PoseStack poseStack, int mouseX, int mouseY, int recipeStartX, int recipeStartY, ItemStack[][] recipe, ItemStack resultStack) {
        // 检查是否悬停在结果物品上
        int resultX = recipeStartX + 40;
        int resultY = recipeStartY + 60;
        if (mouseX >= resultX && mouseX <= resultX + ICON_SIZE && mouseY >= resultY && mouseY <= resultY + ICON_SIZE) {
            renderTooltip(poseStack, resultStack, mouseX, mouseY);
        }
        
        // 检查是否悬停在合成槽上
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = recipeStartX + col * 20;
                int y = recipeStartY + row * 20;
                if (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE) {
                    ItemStack itemStack = recipe[row][col];
                    if (!itemStack.isEmpty()) {
                        // 显示物品的实际名称和信息
                        renderTooltip(poseStack, itemStack, mouseX, mouseY);
                    } else {
                        // 空槽提示
                        renderTooltip(poseStack, Component.literal("空"), mouseX, mouseY);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // 左键点击
            if (!isDetailView) {
                // 检查是否点击了拔刀剑图标
                int startIndex = currentPage * 9;
                int endIndex = Math.min(startIndex + 9, allBlades.size());
                
                // 计算图标区域的居中位置
                int gridWidth = 3 * ICON_SPACING;
                int gridHeight = 3 * ICON_SPACING;
                int gridX = leftPos + (BOOK_WIDTH - gridWidth) / 2;
                int gridY = topPos + (BOOK_HEIGHT - gridHeight) / 2 - 10; // 向上微调10像素
                
                for (int i = startIndex; i < endIndex; i++) {
                    int row = (i - startIndex) / 3;
                    int col = (i - startIndex) % 3;
                    int x = gridX + col * ICON_SPACING;
                    int y = gridY + row * ICON_SPACING;
                    
                    if (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE) {
                        selectedBlade = allBlades.get(i);
                        isDetailView = true;
                        this.init(); // 重新初始化按钮
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    /**
     * 添加所有内置的拔刀剑定义
     */
    private void addAllBuiltinBlades() {
        try {
            SlashBlade.LOGGER.info("Adding all builtin blades directly");
            
            // 添加所有内置的拔刀剑定义
            // 这些拔刀剑定义来自SlashBladeBuiltInRegistry类
            // 我们直接创建这些定义，确保它们能在书中显示
            
            // 鄂门相关
            addBladeDefinition(SlashBlade.prefix("agito"), "model/named/agito_false.png", "model/named/agito.obj", 5.0F, 60);
            addBladeDefinition(SlashBlade.prefix("agito_rust"), "model/named/agito_rust.png", "model/named/agito.obj", 3.0F, 60);
            addBladeDefinition(SlashBlade.prefix("orotiagito"), "model/named/orotiagito.png", "model/named/agito.obj", 7.0F, 60);
            addBladeDefinition(SlashBlade.prefix("orotiagito_sealed"), "model/named/agito_true.png", "model/named/agito.obj", 5.0F, 60);
            addBladeDefinition(SlashBlade.prefix("orotiagito_rust"), "model/named/agito_rust_true.png", "model/named/agito.obj", 3.0F, 60);
            
            // 村正相关
            addBladeDefinition(SlashBlade.prefix("doutanuki"), "model/named/muramasa/doutanuki.png", "model/named/muramasa/muramasa.obj", 5.0F, 60);
            addBladeDefinition(SlashBlade.prefix("muramasa"), "model/named/muramasa/muramasa.png", "model/named/muramasa/muramasa.obj", 7.0F, 50);
            addBladeDefinition(SlashBlade.prefix("sabigatana"), "model/named/muramasa/sabigatana.png", "model/named/muramasa/muramasa.obj", 3.0F, 40);
            
            // 其他拔刀剑
            addBladeDefinition(SlashBlade.prefix("koseki"), "model/named/dios/koseki.png", "model/named/dios/dios.obj", 5.0F, 70);
            addBladeDefinition(SlashBlade.prefix("tagayasan"), "model/named/tagayasan.png", null, 5.0F, 70);
            addBladeDefinition(SlashBlade.prefix("yasha"), "model/named/yasha/yasha.png", "model/named/yasha/yasha.obj", 6.0F, 70);
            addBladeDefinition(SlashBlade.prefix("yasha_true"), "model/named/yasha/yasha.png", "model/named/yasha/yasha_true.obj", 6.0F, 70);
            addBladeDefinition(SlashBlade.prefix("yuzukitukumo"), "model/named/a_tukumo.png", "model/named/agito.obj", 6.0F, 70);
            addBladeDefinition(SlashBlade.prefix("yamato"), "model/named/yamato.png", "model/named/yamato.obj", 7.0F, 70);
            addBladeDefinition(SlashBlade.prefix("sange"), "model/named/sange/sange.png", "model/named/sange/sange.obj", 6.0F, 70);
            addBladeDefinition(SlashBlade.prefix("fox_black"), "model/named/sange/black.png", "model/named/sange/sange.obj", 5.0F, 70);
            addBladeDefinition(SlashBlade.prefix("fox_white"), "model/named/sange/white.png", "model/named/sange/sange.obj", 5.0F, 70);
            
            // 鲁钝系列
            addBladeDefinition(SlashBlade.prefix("rodai_wooden"), "model/rodai_wooden.png", null, 2.0F, 60);
            addBladeDefinition(SlashBlade.prefix("rodai_stone"), "model/rodai_stone.png", null, 3.0F, 132);
            addBladeDefinition(SlashBlade.prefix("rodai_iron"), "model/rodai_iron.png", null, 4.0F, 250);
            addBladeDefinition(SlashBlade.prefix("rodai_golden"), "model/rodai_golden.png", null, 2.0F, 33);
            addBladeDefinition(SlashBlade.prefix("rodai_diamond"), "model/rodai_diamond.png", null, 7.0F, 1561);
            addBladeDefinition(SlashBlade.prefix("rodai_netherite"), "model/rodai_netherite.png", null, 8.0F, 2031);
            
            // 其他
            addBladeDefinition(SlashBlade.prefix("ruby"), "model/ruby.png", null, 5.0F, 45);
            addBladeDefinition(SlashBlade.prefix("rusty_sword"), "model/nameless.png", null, 2.0F, 33);
            
            SlashBlade.LOGGER.info("Added {} builtin blades directly", allBlades.size());
        } catch (Exception e) {
            SlashBlade.LOGGER.warn("Failed to add all builtin blades: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 安全获取物品，防止空指针异常
     */
    private ItemStack safeGetItemStack(ResourceLocation itemId) {
        var item = net.minecraft.core.Registry.ITEM.get(itemId);
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * 添加单个拔刀剑定义
     */
    private void addBladeDefinition(ResourceLocation name, String texturePath, String modelPath, float attackDamage, int maxDamage) {
        try {
            // 使用完整的包名
            mods.flammpfeil.slashblade.registry.slashblade.RenderDefinition.Builder renderBuilder = 
                mods.flammpfeil.slashblade.registry.slashblade.RenderDefinition.Builder.newInstance()
                .standbyRenderType(mods.flammpfeil.slashblade.client.renderer.CarryType.DEFAULT);
            
            // 设置纹理
            renderBuilder.textureName(SlashBlade.prefix(texturePath));
            
            // 设置模型（如果有）
            if (modelPath != null) {
                renderBuilder.modelName(SlashBlade.prefix(modelPath));
            }
            
            // 使用完整的包名
            mods.flammpfeil.slashblade.registry.slashblade.PropertiesDefinition.Builder propertiesBuilder = 
                mods.flammpfeil.slashblade.registry.slashblade.PropertiesDefinition.Builder.newInstance()
                .baseAttackModifier(attackDamage)
                .maxDamage(maxDamage);
            
            SlashBladeDefinition definition = new SlashBladeDefinition(
                name,
                renderBuilder.build(),
                propertiesBuilder.build(),
                java.util.List.of()
            );
            
            allBlades.add(definition);
        } catch (Exception e) {
            SlashBlade.LOGGER.warn("Failed to add blade definition for {}: {}", name, e.getMessage());
        }
    }
}