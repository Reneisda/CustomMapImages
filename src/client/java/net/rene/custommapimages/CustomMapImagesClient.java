package net.rene.custommapimages;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.rene.custommapimages.command.CustomMapCommand;
import net.rene.custommapimages.item.ModItems;

public class CustomMapImagesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModItems.registerModItems();
		CommandRegistrationCallback.EVENT.register(CustomMapCommand::register);

		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}