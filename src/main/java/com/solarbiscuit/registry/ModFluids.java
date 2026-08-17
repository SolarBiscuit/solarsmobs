package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.material.Fluid;

public class ModFluids {
    // Registering the Type (Physics of the liquid) and the Fluid itself
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, SolarsMobs.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, SolarsMobs.MOD_ID);

public static final RegistryObject<FluidType> FEMBOY_MILK_TYPE = FLUID_TYPES.register("femboy_milk", 
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.solarsmobs.femboy_milk")
                    .density(1024)
                    .viscosity(1024)) {
                
                // Lets mods like Mekanism render the fluid in tanks
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions() {
                        
                        @Override
                        @SuppressWarnings("removal")
                        public net.minecraft.resources.ResourceLocation getStillTexture() {
                            return new net.minecraft.resources.ResourceLocation("minecraft:block/water_still");
                        }

                        @Override
                        @SuppressWarnings("removal")
                        public net.minecraft.resources.ResourceLocation getFlowingTexture() {
                            return new net.minecraft.resources.ResourceLocation("minecraft:block/water_flow");
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFFFFFFF; // Pure white for now
                        }
                    });
                }
            });

    public static final RegistryObject<Fluid> FEMBOY_MILK = FLUIDS.register("femboy_milk", 
            () -> new ForgeFlowingFluid.Source(ModFluids.PROPERTIES));
    
    public static final RegistryObject<Fluid> FEMBOY_MILK_FLOWING = FLUIDS.register("femboy_milk_flowing", 
            () -> new ForgeFlowingFluid.Flowing(ModFluids.PROPERTIES));

    // Links the fluid to your specific bucket item
    public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
            FEMBOY_MILK_TYPE, FEMBOY_MILK, FEMBOY_MILK_FLOWING)
            .bucket(ModItems.FEMBOY_MILK_BUCKET);
}