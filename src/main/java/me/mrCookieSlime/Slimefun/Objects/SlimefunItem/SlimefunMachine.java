package me.mrCookieSlime.Slimefun.Objects.SlimefunItem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlock;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.MultiBlockMachine;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;

/**
 * 
 * @deprecated Please use {@link MultiBlockMachine} instead.
 *
 */
@Deprecated
public class SlimefunMachine extends SlimefunItem implements RecipeDisplayItem {

    private final MultiBlock multiblock;

    protected SlimefunMachine(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        multiblock = null;
    }

    public SlimefunMachine(Category category, ItemStack item, String id, ItemStack[] recipe, BlockFace trigger) {
        super(category, item, id, RecipeType.MULTIBLOCK, recipe);
        this.multiblock = new MultiBlock(this, convertItemStacksToMaterial(recipe), trigger);
    }

    public List<ItemStack[]> getRecipes() {
        return new ArrayList<>();
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        return new ArrayList<>();
    }

    public void addRecipe(ItemStack[] input, ItemStack output) {
        // Moved to MultiblockMachine
    }

    @Override
    public void postRegister() {
        if (multiblock != null) {
            SlimefunPlugin.getRegistry().getMultiBlocks().add(multiblock);
        }
    }

    private static Material[] convertItemStacksToMaterial(ItemStack[] items) {
        List<Material> materials = new ArrayList<>();

        for (ItemStack item : items) {
            if (item == null) {
                materials.add(null);
            }
            else if (item.getType() == Material.FLINT_AND_STEEL) {
                materials.add(Material.FIRE);
            }
            else {
                materials.add(item.getType());
            }
        }

        return materials.toArray(new Material[0]);
    }

    public MultiBlock getMultiBlock() {
        return multiblock;
    }
}
