package com.phasepal.phasepulse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

/**
 * Handles the /pal command for sending messages to Phase Pal.
 *
 * Commands:
 * - /pal <message> - Send a chat message to Phase Pal
 * - /pal speak <message> - Send a message for Phase Pal to speak aloud
 *
 * Security features:
 * - Only captures messages from the local player who explicitly types the command
 * - Does not capture public chat or other players' messages
 * - Rate limited to prevent spam (configurable cooldown)
 * - Input sanitization to remove control characters and escape sequences
 * - Configurable maximum message length
 * - Can be disabled via config
 */
public class PalCommand {

    private static final String EVENT_CHAT_MESSAGE = "chat_message";

    // Rate limiting
    private static long lastCommandTime = 0;

    // Pattern to match control characters (except common whitespace)
    // Matches: null bytes, control chars 0x00-0x1F (except tab, newline, carriage return), 0x7F, and escape sequences
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    /**
     * Registers the /pal command with the client command system.
     */
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(PalCommand::registerCommands);
        PhasePulse.LOGGER.info("Registered /pal command");
    }

    private static void registerCommands(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {

        dispatcher.register(
            ClientCommandManager.literal("pal")
                // /pal speak <message> - for text-to-speech
                .then(ClientCommandManager.literal("speak")
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeSpeakCommand(context))))
                // /pal <message> - regular chat message
                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> executeMessageCommand(context)))
                // /pal with no arguments - show usage
                .executes(context -> executeHelp(context))
        );
    }

    /**
     * Handles /pal <message> - sends a chat message to Phase Pal.
     */
    private static int executeMessageCommand(CommandContext<FabricClientCommandSource> context) {
        String message = StringArgumentType.getString(context, "message");
        return sendChatMessage(context, message, false);
    }

    /**
     * Handles /pal speak <message> - sends a message for Phase Pal to speak.
     */
    private static int executeSpeakCommand(CommandContext<FabricClientCommandSource> context) {
        String message = StringArgumentType.getString(context, "message");
        return sendChatMessage(context, message, true);
    }

    /**
     * Sends the chat message event to Phase Pal.
     *
     * @param context Command context
     * @param message The message text
     * @param speak Whether Phase Pal should speak the response
     * @return 1 on success, 0 on failure
     */
    private static int sendChatMessage(
            CommandContext<FabricClientCommandSource> context,
            String message,
            boolean speak) {

        PhasePulseConfig config = ConfigManager.getConfig();

        // Check if chat commands are enabled
        if (!config.sendChatCommands) {
            context.getSource().sendError(Text.literal("Chat commands are disabled in config"));
            return 0;
        }

        // Rate limiting - check cooldown
        long now = System.currentTimeMillis();
        long timeSinceLastCommand = now - lastCommandTime;
        if (timeSinceLastCommand < config.chatCommandCooldownMs) {
            long remainingMs = config.chatCommandCooldownMs - timeSinceLastCommand;
            context.getSource().sendError(Text.literal(
                    "Please wait " + (remainingMs / 1000.0) + "s before sending another message"));
            return 0;
        }

        // Validate message
        if (message == null || message.trim().isEmpty()) {
            context.getSource().sendError(Text.literal("Message cannot be empty"));
            return 0;
        }

        // Sanitize input: remove control characters and escape sequences
        String sanitizedMessage = sanitizeInput(message);

        // Trim after sanitization
        String trimmedMessage = sanitizedMessage.trim();

        // Check if message is empty after sanitization
        if (trimmedMessage.isEmpty()) {
            context.getSource().sendError(Text.literal("Message cannot be empty"));
            return 0;
        }

        // Check message length (use configurable limit)
        if (trimmedMessage.length() > config.maxChatMessageLength) {
            context.getSource().sendError(Text.literal(
                    "Message too long (max " + config.maxChatMessageLength + " characters)"));
            return 0;
        }

        // Update rate limiting timestamp
        lastCommandTime = now;

        // Check if network is available
        NetworkManager network = NetworkManager.getInstance();
        if (!network.isConnected()) {
            context.getSource().sendFeedback(Text.literal("§7[Phase Pal] Connecting..."));
        }

        // Create and send the event packet
        EventPacket packet = new EventPacket(EVENT_CHAT_MESSAGE)
                .addMetadata("message", trimmedMessage)
                .addMetadata("speak", speak);

        network.sendEvent(packet);

        // Provide feedback to the player
        String feedbackMessage = speak
                ? "§7[Phase Pal] §fSpeaking: §7" + trimmedMessage
                : "§7[Phase Pal] §f" + trimmedMessage;
        context.getSource().sendFeedback(Text.literal(feedbackMessage));

        PhasePulse.LOGGER.debug("Sent chat message to Phase Pal: {} (speak={})", trimmedMessage, speak);

        return 1;
    }

    /**
     * Sanitizes user input by removing potentially dangerous characters.
     * Removes control characters, null bytes, and normalizes whitespace.
     *
     * @param input The raw user input
     * @return Sanitized string safe for transmission
     */
    private static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // Remove control characters (except tab, newline, carriage return which are handled by trim)
        String sanitized = CONTROL_CHARS.matcher(input).replaceAll("");

        // Normalize multiple spaces to single space
        sanitized = sanitized.replaceAll("\\s+", " ");

        return sanitized;
    }

    /**
     * Shows usage help for /pal command.
     */
    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("§6Phase Pal Commands:"));
        context.getSource().sendFeedback(Text.literal("§7/pal <message> §f- Send a message to Phase Pal"));
        context.getSource().sendFeedback(Text.literal("§7/pal speak <message> §f- Phase Pal speaks the response"));
        return 1;
    }
}
