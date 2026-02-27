package com.phasepal.phasepulse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.regex.Pattern;

/**
 * Handles the /pal command for sending messages to Phase Pal.
 */
public final class PalCommand {
	private static final String EVENT_USER_CHAT = "user_chat";
	private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
	private static long lastCommandTime = 0;
	private static boolean registered = false;

	private PalCommand() {
	}

	public static void register() {
		if (registered) {
			return;
		}

		MinecraftForge.EVENT_BUS.addListener(PalCommand::onRegisterClientCommands);
		registered = true;
		PhasePulse.LOGGER.info("Registered /pal command");
	}

	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		dispatcher.register(
				Commands.literal("pal")
						.then(Commands.literal("speak")
								.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(PalCommand::executeSpeakCommand)))
						.then(Commands.literal("chat")
								.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(PalCommand::executeMessageCommand)))
						.then(Commands.argument("message", StringArgumentType.greedyString())
								.executes(PalCommand::executeMessageCommand))
						.executes(PalCommand::executeHelp)
		);
	}

	private static int executeMessageCommand(CommandContext<CommandSourceStack> context) {
		String message = StringArgumentType.getString(context, "message");
		return sendMessage(context, message, false);
	}

	private static int executeSpeakCommand(CommandContext<CommandSourceStack> context) {
		String message = StringArgumentType.getString(context, "message");
		return sendMessage(context, message, true);
	}

	private static int sendMessage(CommandContext<CommandSourceStack> context, String message, boolean speak) {
		PhasePulseConfig config = ConfigManager.getConfig();
		CommandSourceStack source = context.getSource();

		if (!config.sendChatMessages) {
			source.sendFailure(Component.literal("Chat to Phase Pal is disabled in config"));
			return 0;
		}

		long now = System.currentTimeMillis();
		long timeSinceLastCommand = now - lastCommandTime;
		if (timeSinceLastCommand < config.chatCommandCooldownMs) {
			long remainingMs = config.chatCommandCooldownMs - timeSinceLastCommand;
			double remainingSec = remainingMs / 1000.0;
			source.sendFailure(Component.literal(String.format("Please wait %.1fs before sending another message", remainingSec)));
			return 0;
		}

		if (message == null || message.trim().isEmpty()) {
			source.sendFailure(Component.literal("Message cannot be empty"));
			return 0;
		}

		String sanitized = sanitizeInput(message);
		if (sanitized.isEmpty()) {
			source.sendFailure(Component.literal("Message cannot be empty"));
			return 0;
		}

		if (sanitized.length() > config.maxChatMessageLength) {
			source.sendFailure(Component.literal("Message too long (max " + config.maxChatMessageLength + " characters)"));
			return 0;
		}

		lastCommandTime = now;

		NetworkManager network = NetworkManager.getInstance();
		if (!network.isConnected()) {
			source.sendSuccess(() -> Component.literal("[Phase Pal] Connecting..."), false);
		}

		EventPacket packet = new EventPacket(EVENT_USER_CHAT)
				.addMetadata("text", sanitized)
				.addMetadata("speak", speak);
		network.sendEvent(packet);

		String feedback = speak ? "[Phase Pal] Speaking: " + sanitized : "[Phase Pal] " + sanitized;
		source.sendSuccess(() -> Component.literal(feedback), false);

		if (config.debugLogging) {
			PhasePulse.LOGGER.debug("Sent to Phase Pal: {} (speak={})", sanitized, speak);
		}

		return 1;
	}

	private static String sanitizeInput(String input) {
		if (input == null) {
			return "";
		}

		String sanitized = CONTROL_CHARS.matcher(input).replaceAll("");
		return sanitized.replaceAll("\\s+", " ").trim();
	}

	private static int executeHelp(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		source.sendSuccess(() -> Component.literal("Phase Pal commands:"), false);
		source.sendSuccess(() -> Component.literal("/pal chat <message> - Send a message to Phase Pal"), false);
		source.sendSuccess(() -> Component.literal("/pal speak <message> - Phase Pal speaks the response"), false);
		source.sendSuccess(() -> Component.literal("Tip: You can also use /pal <message> directly"), false);
		return 1;
	}
}
