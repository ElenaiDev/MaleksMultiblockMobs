package com.malekire.multiblockmobs.blocks;

import java.util.Random;
import java.util.Vector;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.malekire.multiblockmobs.Main;
import com.malekire.multiblockmobs.particle.EobEnumParticleTypes;
import com.malekire.multiblockmobs.particle.ParticleSpawner;
import com.malekire.multiblockmobs.util.ModChecker;
import com.malekire.multiblockmobs.util.Reference;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoulChassis extends Block {
    private BlockPattern snowmanBasePattern;
    private BlockPattern snowmanPattern;
    private BlockPattern golemBasePattern;
    private BlockPattern golemPattern;
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public static final AxisAlignedBB SOUL_CHASSIS_AABB = new AxisAlignedBB(0.25D, 0D, 0.25D, 0.75D, 1, 0.75D);
    public Vector<BlockPattern> blockPatterns = new Vector<BlockPattern>();
    private static final Predicate<IBlockState> IS_PUMPKIN = new Predicate<IBlockState>() {
        public boolean apply(@Nullable IBlockState p_apply_1_) {
            return p_apply_1_ != null
                    && (p_apply_1_.getBlock() == ModBlocks.soulChassis || p_apply_1_.getBlock() == Blocks.PUMPKIN);
        }
    };

    BlockPos blockPos1 = null;

    protected SoulChassis() {
        super(Material.GOURD, MapColor.ADOBE);
        // super();
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

        this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "soul_chassis"));
        this.setUnlocalizedName(this.getRegistryName().toString());
        
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

    }

    @Override
	protected BlockStateContainer createBlockState() {
		 return new BlockStateContainer(this, new IProperty[] { FACING });
	}
    
    @Override
	public IBlockState getStateFromMeta(int meta) {
		
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);

	        return this.getDefaultState().withProperty(FACING, enumfacing);
	}
	@Override
	public int getMetaFromState(IBlockState state) {
		
		return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
	}

    @Override
    public boolean hasTileEntity(IBlockState state) {

        return hasTileEntity;

    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;

    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity
     * is set
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            blockPos1 = pos;

            
            this.trySpawnMultiblockMob(worldIn, pos);
            this.trySpawnGolem(worldIn, pos);
            // blockMatcher(worldIn, pos, new SoulChassis());
            return true;
        } else
            return false;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    	 this.setDefaultFacing(worldIn, pos, state);

    }
    private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            IBlockState iblockstate = worldIn.getBlockState(pos.north());
            IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
            IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
            IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
            EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);

            if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock())
            {
                enumfacing = EnumFacing.SOUTH;
            }
            else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock())
            {
                enumfacing = EnumFacing.NORTH;
            }
            else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock())
            {
                enumfacing = EnumFacing.EAST;
            }
            else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock())
            {
                enumfacing = EnumFacing.WEST;
            }

            worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

    }
    
    public boolean blockMatcher(World worldIn, BlockPos pos, Block matcher) {

        System.out.println(Block.isEqualTo(worldIn.getBlockState(pos).getBlock(),
                Block.getBlockFromName(matcher.getLocalizedName())));
        return true;
    }

    public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
        ParticleSpawner.spawnParticle(EobEnumParticleTypes.FLOWER, blockPos1.getX() + .5, blockPos1.getY() + 1,
                blockPos1.getZ() + .5, 0.0D, -1.0D, 0.0D);
    }

    public boolean canDispenserPlace(World worldIn, BlockPos pos) {
        return this.getSnowmanBasePattern().match(worldIn, pos) != null
                || this.getGolemBasePattern().match(worldIn, pos) != null;
    }

    private void trySpawnMultiblockMob(World worldIn, BlockPos pos) {
        boolean trueSuccess = false;
        BlockPos blockpos1 = null;
        for (int i6 = 0; i6 < Main.blockPatternContainer.size(); i6++) {
        	if(Main.dimension.get(i6) == null || worldIn.provider.getDimension() == Main.dimension.get(i6)) {
        	
            boolean blockSuccess = true;
            int rotation = 0;
            for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {

                BlockPattern theBlockPattern;
                theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);
                // System.out.println(Main.blockPatternContainer.get(i6).size());

                BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                        new BlockPos(pos.getX(), pos.getY(), pos.getZ() + i5));

                if (blockpattern$patternhelper != null) {

                    blockpos1 = blockpattern$patternhelper.translateOffset(0, 2, 0).getPos();
                } else {
                    blockSuccess = false;
                    rotation++;
                }
            }

            if (blockSuccess) {
                trueSuccess = true;

                for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {
                    BlockPattern theBlockPattern;
                    theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                    BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                            new BlockPos(pos.getX(), pos.getY(), pos.getZ() + i5));
                    for (int j = 0; j < theBlockPattern.getPalmLength(); ++j) {
                        for (int k = 0; k < theBlockPattern.getThumbLength(); ++k) {
                            worldIn.setBlockState(blockpattern$patternhelper.translateOffset(j, k, 0).getPos(),
                                    Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            } else {
                blockSuccess = true;
                for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {

                    BlockPattern theBlockPattern;
                    theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                    BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                            new BlockPos(pos.getX(), pos.getY(), pos.getZ() - i5));

                    if (blockpattern$patternhelper != null) {
                        blockpos1 = blockpattern$patternhelper.translateOffset(0, 2, 0).getPos();

                    } else {
                        blockSuccess = false;
                        rotation++;
                    }
                }

                if (blockSuccess) {
                    trueSuccess = true;

                    for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {
                        BlockPattern theBlockPattern;
                        theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                        BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                                new BlockPos(pos.getX(), pos.getY(), pos.getZ() - i5));
                        for (int j = 0; j < theBlockPattern.getPalmLength(); ++j) {
                            for (int k = 0; k < theBlockPattern.getThumbLength(); ++k) {
                                worldIn.setBlockState(blockpattern$patternhelper.translateOffset(j, k, 0).getPos(),
                                        Blocks.AIR.getDefaultState(), 2);
                            }
                        }
                    }
                } else {
                    blockSuccess = true;
                    for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {

                        BlockPattern theBlockPattern;
                        theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                        BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                                new BlockPos(pos.getX() + i5, pos.getY(), pos.getZ()));

                        if (blockpattern$patternhelper != null) {
                            blockpos1 = blockpattern$patternhelper.translateOffset(0, 2, 0).getPos();

                        } else {
                            blockSuccess = false;
                            rotation++;
                        }
                    }

                    if (blockSuccess) {
                        trueSuccess = true;
                        for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {
                            BlockPattern theBlockPattern;
                            theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                            BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                                    new BlockPos(pos.getX() + i5, pos.getY(), pos.getZ()));
                            for (int j = 0; j < theBlockPattern.getPalmLength(); ++j) {
                                for (int k = 0; k < theBlockPattern.getThumbLength(); ++k) {
                                    worldIn.setBlockState(blockpattern$patternhelper.translateOffset(j, k, 0).getPos(),
                                            Blocks.AIR.getDefaultState(), 2);
                                }
                            }
                        }
                    } else {
                        blockSuccess = true;
                        for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {

                            BlockPattern theBlockPattern;
                            theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                            BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                                    new BlockPos(pos.getX() - i5, pos.getY(), pos.getZ()));

                            if (blockpattern$patternhelper != null) {
                                blockpos1 = blockpattern$patternhelper.translateOffset(0, 2, 0).getPos();

                            } else {
                                blockSuccess = false;
                                rotation++;
                            }
                        }

                        if (blockSuccess) {
                            trueSuccess = true;
                            for (int i5 = 0; i5 < Main.blockPatternContainer.get(i6).size(); i5++) {
                                BlockPattern theBlockPattern;
                                theBlockPattern = Main.blockPatternContainer.get(i6).get(i5);

                                BlockPattern.PatternHelper blockpattern$patternhelper = theBlockPattern.match(worldIn,
                                        new BlockPos(pos.getX() - i5, pos.getY(), pos.getZ()));
                                for (int j = 0; j < theBlockPattern.getPalmLength(); ++j) {
                                    for (int k = 0; k < theBlockPattern.getThumbLength(); ++k) {
                                        worldIn.setBlockState(
                                                blockpattern$patternhelper.translateOffset(j, k, 0).getPos(),
                                                Blocks.AIR.getDefaultState(), 2);
                                    }
                                }
                            }
                        }

                    }
                }
            }
            if (trueSuccess) {

                Entity spawnedMob = ForgeRegistries.ENTITIES
                        .getValue(new ResourceLocation(Main.entities.get(i6).modID, Main.entities.get(i6).thing))
                        .newInstance(worldIn);
                spawnedMob.setLocationAndAngles((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.05D,
                        (double) blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
                worldIn.spawnEntity(spawnedMob);
                for (int i9 = 0; i9 < Main.commands.size(); i9++) {
                    if (Main.commands.get(i9).place == i6) {
                        spawnedMob.getServer().commandManager.executeCommand(spawnedMob, Main.commands.get(i9).command);
                    }
                }
                trueSuccess = false;
            }
        }
        }

    }

    private void trySpawnGolem(World worldIn, BlockPos pos) {
        BlockPattern.PatternHelper blockpattern$patternhelper = this.getSnowmanPattern().match(worldIn, pos);

        if (blockpattern$patternhelper != null) {
            for (int i = 0; i < this.getSnowmanPattern().getThumbLength(); ++i) {
                BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(0, i, 0);
                worldIn.setBlockState(blockworldstate.getPos(), Blocks.AIR.getDefaultState(), 2);
            }

            BlockPos blockpos1 = blockpattern$patternhelper.translateOffset(0, 2, 0).getPos();
            if (ModChecker.isHarvesterLoaded) {

                Entity harvester = ForgeRegistries.ENTITIES
                        .getValue(new ResourceLocation("harvestersnight", "harvester")).newInstance(worldIn);
                harvester.setLocationAndAngles((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.05D,
                        (double) blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
                worldIn.spawnEntity(harvester);
                harvester.playSound(Main.harvesterLaugh, 10, 1);
                for (EntityPlayerMP entityplayermp : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class,
                        harvester.getEntityBoundingBox().grow(5.0D))) {
                    entityplayermp.playSound(Main.harvesterLaugh, (float) 100.0, (float) 1.0);
                    entityplayermp.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10, 10, false, false));
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp, harvester);
                }

            }
            for (int l = 0; l < 120; ++l) {
                worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL,
                        (double) blockpos1.getX() + worldIn.rand.nextDouble(),
                        (double) blockpos1.getY() + worldIn.rand.nextDouble() * 2.5D,
                        (double) blockpos1.getZ() + worldIn.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

            for (int i1 = 0; i1 < this.getSnowmanPattern().getThumbLength(); ++i1) {
                BlockWorldState blockworldstate2 = blockpattern$patternhelper.translateOffset(0, i1, 0);
                worldIn.notifyNeighborsRespectDebug(blockworldstate2.getPos(), Blocks.AIR, false);
            }
        } else {
            blockpattern$patternhelper = this.getGolemPattern().match(worldIn, pos);

            if (blockpattern$patternhelper != null) {
                for (int j = 0; j < this.getGolemPattern().getPalmLength(); ++j) {
                    for (int k = 0; k < this.getGolemPattern().getThumbLength(); ++k) {
                        worldIn.setBlockState(blockpattern$patternhelper.translateOffset(j, k, 0).getPos(),
                                Blocks.AIR.getDefaultState(), 2);
                    }
                }

                BlockPos blockpos = blockpattern$patternhelper.translateOffset(1, 2, 0).getPos();
                EntitySheep entityirongolem = new EntitySheep(worldIn);
                entityirongolem.setLocationAndAngles((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.05D,
                        (double) blockpos.getZ() + 0.5D, 0.0F, 0.0F);
                worldIn.spawnEntity(entityirongolem);

                for (EntityPlayerMP entityplayermp1 : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class,
                        entityirongolem.getEntityBoundingBox().grow(5.0D))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp1, entityirongolem);
                }

                for (int j1 = 0; j1 < 120; ++j1) {
                    worldIn.spawnParticle(EnumParticleTypes.SNOWBALL,
                            (double) blockpos.getX() + worldIn.rand.nextDouble(),
                            (double) blockpos.getY() + worldIn.rand.nextDouble() * 3.9D,
                            (double) blockpos.getZ() + worldIn.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
                }

                for (int k1 = 0; k1 < this.getGolemPattern().getPalmLength(); ++k1) {
                    for (int l1 = 0; l1 < this.getGolemPattern().getThumbLength(); ++l1) {
                        BlockWorldState blockworldstate1 = blockpattern$patternhelper.translateOffset(k1, l1, 0);
                        worldIn.notifyNeighborsRespectDebug(blockworldstate1.getPos(), Blocks.AIR, false);
                    }
                }
            }
        }
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return true;
    }

    protected BlockPattern getSnowmanBasePattern() {
        if (this.snowmanBasePattern == null) {
            this.snowmanBasePattern = FactoryBlockPattern.start().aisle(" ", "#", "#")
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SNOW))).build();
        }

        return this.snowmanBasePattern;
    }

    protected BlockPattern getSnowmanPattern() {
        if (this.snowmanPattern == null) {
            this.snowmanPattern = FactoryBlockPattern.start().aisle("^", "#", "#")
                    .where('^', BlockWorldState.hasState(IS_PUMPKIN))
                    .where('#', Main.getBlock("minecraft", "hay_block")).build();
        }

        return this.snowmanPattern;
    }

    protected BlockPattern getGolemBasePattern() {
        if (this.golemBasePattern == null) {
            this.golemBasePattern = FactoryBlockPattern.start().aisle("~ ~", "###", "~#~")
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.WOOL)))
                    .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        }

        return this.golemBasePattern;
    }

    protected BlockPattern getGolemPattern() {
        if (this.golemPattern == null) {
            this.golemPattern = FactoryBlockPattern.start().aisle("~^~", "###", "~#~")
                    .where('^', BlockWorldState.hasState(IS_PUMPKIN))
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.WOOL)))
                    .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        }

        return this.golemPattern;
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SOUL_CHASSIS_AABB;

    }
}
