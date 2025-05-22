package net.rene.custommapimages;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.rene.custommapimages.command.CustomMapColors;
import net.rene.custommapimages.command.CustomMapCommand;
import net.rene.custommapimages.command.ImageBlender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomMapImages implements ModInitializer {
	public static final String MOD_ID = "custommapimages";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		try {
			Class.forName(CustomMapColors.class.getName());			// preload colors
			Class.forName(ImageBlender.class.getName());			// preload blending
		} catch (ClassNotFoundException e) {
			LOGGER.error("Could not initialize class");
		}

		CommandRegistrationCallback.EVENT.register(CustomMapCommand::register);
	}
}