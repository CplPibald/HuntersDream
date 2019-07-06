package theblockbox.huntersdream.blocks;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import theblockbox.huntersdream.Main;
import theblockbox.huntersdream.api.init.PropertyInit;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class BlockGarland extends Block {
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.05D, 1.0D, 1.0D, 0.05D);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.95D, 1.0D, 1.0D, 0.95D);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.05D, 0.0D, 0.0D, 0.05D, 1.0D, 1.0D);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.95D, 0.0D, 0.0D, 0.95D, 1.0D, 1.0D);

    static {
        for (int i = 0; i < PropertyInit.GARLAND_PROPERTIES.length; i++) {
            if (!PropertyInit.GARLAND_PROPERTIES[i].getName().equals(EnumFacing.byIndex(i + 2).toString())) {
                Main.getLogger().warn("The array BlockGarland.PROPERTIES is in the order "
                        + Arrays.toString(PropertyInit.GARLAND_PROPERTIES) + ", while the array EnumFacing.VALUES is in the order "
                        + Arrays.toString(EnumFacing.VALUES) + ". This is a bug and could lead to problems with " +
                        "Hunter's Dream's garlands. If you see this message, please open a new issue on our issue tracker!");
                break;
            }
        }
    }

    public BlockGarland() {
        super(Material.PLANTS);
        this.setSoundType(SoundType.PLANT);
        IBlockState defaultState = this.getDefaultState();
        for (PropertyBool property : PropertyInit.GARLAND_PROPERTIES) {
            defaultState = defaultState.withProperty(property, false);
        }
        this.setDefaultState(defaultState);
    }

    /**
     * Don't override outside of hunter's dream
     */
    public BlockGarland getDefault() {
        return this;
    }

    public boolean isTheSameAs(BlockGarland otherBlock) {
        return otherBlock.getDefault() == this.getDefault();
    }

    public boolean isAllowedNeighbor(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
        IBlockState state = blockAccess.getBlockState(pos);
        return (state.getBlockFaceShape(blockAccess, pos, facing) == BlockFaceShape.SOLID) || (state.getProperties()
                .containsKey(BlockDoor.FACING) && (state.getValue(BlockDoor.FACING) == facing.getOpposite()));
    }

    public boolean isAllowedState(IBlockState state) {
        if ((state.getBlock() instanceof BlockGarland) && this.isTheSameAs((BlockGarland) state.getBlock())) {
            for (PropertyBool property : PropertyInit.GARLAND_PROPERTIES) {
                if (state.getValue(property)) {
                    return true;
                }
            }
            return false;
        }
        throw new IllegalArgumentException("The block of the passed blockstate (" + state + ") is not the same as " + this);
    }

    public Object2IntMap.Entry<IBlockState> checkSides(IBlockAccess blockAccess, BlockPos posIn) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posIn);
        IBlockState state = blockAccess.getBlockState(posIn);
        int removedGarlands = 0;

        IBlockState stateToReturn = this.getDefaultState();
        for (PropertyBool property : PropertyInit.GARLAND_PROPERTIES) {
            if (state.getValue(property)) {
                EnumFacing facing = BlockGarland.getFacingFromProperty(property);
                if (facing != null) {
                    EnumFacing opposite = facing.getOpposite();
                    if (this.isAllowedNeighbor(blockAccess, pos.move(facing), opposite)) {
                        stateToReturn = stateToReturn.withProperty(property, true);
                    } else {
                        removedGarlands++;
                    }
                    pos.move(opposite);
                }
            }
        }
        return new AbstractObject2IntMap.BasicEntry<>(stateToReturn, removedGarlands);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this.getDefault());
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this.getDefault());
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        int toReturn = 0;
        for (PropertyBool property : PropertyInit.GARLAND_PROPERTIES)
            if (state.getValue(property))
                toReturn++;
        return toReturn;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PropertyInit.GARLAND_PROPERTIES);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState toReturn = this.getDefaultState();
        for (int i = 0; i < PropertyInit.GARLAND_PROPERTIES.length; i++) {
            toReturn = toReturn.withProperty(PropertyInit.GARLAND_PROPERTIES[i], (meta & (1 << i)) != 0);
        }
        return toReturn;
    }

    @Override
    public final int getMetaFromState(IBlockState state) {
        int toReturn = 0;
        for (int i = 0; i < PropertyInit.GARLAND_PROPERTIES.length; i++) {
            toReturn |= (state.getValue(PropertyInit.GARLAND_PROPERTIES[i]) ? 1 : 0) << i;
        }
        return toReturn;
    }

    @Nullable
    public static EnumFacing getFacingFromProperty(PropertyBool property) {
        for (int i = 0; i < PropertyInit.GARLAND_PROPERTIES.length; i++) {
            if (PropertyInit.GARLAND_PROPERTIES[i] == property) {
                return EnumFacing.byIndex(i + 2);
            }
        }
        return null;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        switch (rot) {
            case CLOCKWISE_90:
                return state.withProperty(PropertyInit.GARLAND_NORTH, state.getValue(PropertyInit.GARLAND_EAST))
                        .withProperty(PropertyInit.GARLAND_EAST, state.getValue(PropertyInit.GARLAND_SOUTH))
                        .withProperty(PropertyInit.GARLAND_SOUTH, state.getValue(PropertyInit.GARLAND_WEST))
                        .withProperty(PropertyInit.GARLAND_WEST, state.getValue(PropertyInit.GARLAND_NORTH));
            case CLOCKWISE_180:
                return state.withProperty(PropertyInit.GARLAND_NORTH, state.getValue(PropertyInit.GARLAND_SOUTH))
                        .withProperty(PropertyInit.GARLAND_EAST, state.getValue(PropertyInit.GARLAND_WEST))
                        .withProperty(PropertyInit.GARLAND_SOUTH, state.getValue(PropertyInit.GARLAND_NORTH))
                        .withProperty(PropertyInit.GARLAND_WEST, state.getValue(PropertyInit.GARLAND_EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(PropertyInit.GARLAND_NORTH, state.getValue(PropertyInit.GARLAND_WEST))
                        .withProperty(PropertyInit.GARLAND_EAST, state.getValue(PropertyInit.GARLAND_NORTH))
                        .withProperty(PropertyInit.GARLAND_SOUTH, state.getValue(PropertyInit.GARLAND_EAST))
                        .withProperty(PropertyInit.GARLAND_WEST, state.getValue(PropertyInit.GARLAND_SOUTH));
            default:
                return state;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        // TODO: When updating to 1.13, check if the array still has the same ordering
        IBlockState oldState = world.getBlockState(pos);
        return (((oldState.getBlock() instanceof BlockGarland) && this.isTheSameAs((BlockGarland) oldState.getBlock()))
                ? oldState : this.getDefault().getDefaultState()).withProperty(
                PropertyInit.GARLAND_PROPERTIES[facing.getOpposite().getIndex() - 2], true);
    }


    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return Block.NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB toReturn = Block.FULL_BLOCK_AABB;
        int properties = 0;
        if (state.getValue(PropertyInit.GARLAND_NORTH)) {
            toReturn = BlockGarland.NORTH_AABB;
            properties++;
        }
        if (state.getValue(PropertyInit.GARLAND_SOUTH)) {
            toReturn = BlockGarland.SOUTH_AABB;
            properties++;
        }
        if (state.getValue(PropertyInit.GARLAND_WEST)) {
            toReturn = BlockGarland.WEST_AABB;
            properties++;
        }
        if (state.getValue(PropertyInit.GARLAND_EAST)) {
            toReturn = BlockGarland.EAST_AABB;
            properties++;
        }
        return (properties == 1) ? toReturn : Block.FULL_BLOCK_AABB;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            Object2IntMap.Entry<IBlockState> returned = this.checkSides(worldIn, pos);
            IBlockState returnedState = returned.getKey();
            if (returnedState != state)
                // TODO: Test if this still works in 1.13/1.14 (because of the air)
                worldIn.setBlockState(pos, this.isAllowedState(returnedState) ? returnedState : Blocks.AIR.getDefaultState());
            if (returned.getIntValue() > 0)
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY() - 0.5D, pos.getZ(),
                        new ItemStack(this.getDefault(), returned.getIntValue()));
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        if (this.isAllowedNeighbor(worldIn, pos.offset(side.getOpposite()), side)) {
            if (super.canPlaceBlockOnSide(worldIn, pos, side) && !((side == EnumFacing.UP) || (side == EnumFacing.DOWN))) {
                return true;
            } else {
                IBlockState state = worldIn.getBlockState(pos);
                if (state.getBlock() instanceof BlockGarland) {
                    return this.isTheSameAs((BlockGarland) state.getBlock())
                            && !((side == EnumFacing.UP) || (side == EnumFacing.DOWN))
                            && !state.getValue(PropertyInit.GARLAND_PROPERTIES[side.getOpposite().getIndex() - 2]);
                }
            }
        }
        return false;
    }
}
