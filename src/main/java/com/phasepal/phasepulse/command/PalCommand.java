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
 * - Input sanitization to remove control characters
 * - Rate limited to prevent spam (configurable cooldown)
 * - Configurable maximum message length
 * - Can be disabled via config
 */
public class PalCommand {

    private static final String EVENT_USER_CHAT = "user_chat";

    // Rate limiting
    private static long lastCommandTime = 0;

    // Pattern to match control characters (except common whitespace)
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
                // /pal chat <message> - explicit chat sub-command for discoverability
                .then(ClientCommandManager.literal("chat")
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeMessageCommand(context))))
                // /pal <message> - regular chat message (fallback/power user)
                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                    .suggests((context, builder) -> builder.suggest("<message>").buildFuture())
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
        return sendMessage(context, message, false);
    }

    /**
     * Handles /pal speak <message> - sends a message for Phase Pal to speak.
     */
    private static int executeSpeakCommand(CommandContext<FabricClientCommandSource> context) {
        String message = StringArgumentType.getString(context, "message");
        return sendMessage(context, message, true);
    }

    /**
     * Sends the message event to Phase Pal.
     *
     * @param context Command context
     * @param message The message text
     * @param speak Whether Phase Pal should speak the response
     * @return 1 on success, 0 on failure
     */
    private static int sendMessage(
            CommandContext<FabricClientCommandSource> context,
            String message,
            boolean speak) {

        PhasePulseConfig config = ConfigManager.getConfig();
        FabricClientCommandSource source = context.getSource();

        // Check if chat messages are enabled
        if (!config.sendChatMessages) {
            source.sendError(Text.literal("Chat to Phase Pal is disabled in config"));
            return 0;
        }

        // Rate limiting check
        long now = System.currentTimeMillis();
        long timeSinceLastCommand = now - lastCommandTime;
        if (timeSinceLastCommand < config.chatCommandCooldownMs) {
            long remainingMs = config.chatCommandCooldownMs - timeSinceLastCommand;
            double remainingSec = remainingMs / 1000.0;
            source.sendError(Text.literal(
                    String.format("Please wait %.1fs before sending another message", remainingSec)));
            return 0;
        }

        // Validate message not empty
        if (message == null || message.trim().isEmpty()) {
            source.sendError(Text.literal("Message cannot be empty"));
            return 0;
        }

        // Sanitize input
        String sanitized = sanitizeInput(message);

        // Check if message is empty after sanitization
        if (sanitized.isEmpty()) {
            source.sendError(Text.literal("Message cannot be empty"));
            return 0;
        }

        // Check message length
        if (sanitized.length() > config.maxChatMessageLength) {
            source.sendError(Text.literal(
                    "Message too long (max " + config.maxChatMessageLength + " characters)"));
            return 0;
        }

        // Update rate limiting timestamp
        lastCommandTime = now;

        // Check connection status and notify user
        NetworkManager network = NetworkManager.getInstance();
        if (!network.isConnected()) {
            source.sendFeedback(Text.literal("\u00a77[Phase Pal] Connecting..."));
        }

        // Create and send the event packet
        EventPacket packet = new EventPacket(EVENT_USER_CHAT)
                .addMetadata("text", sanitized)
                .addMetadata("speak", speak);

        network.sendEvent(packet);

        // Provide feedback to the player
        String feedbackMessage = speak
                ? "\u00a77[Phase Pal] \u00a7fSpeaking: \u00a77" + sanitized
                : "\u00a77[Phase Pal] \u00a7f" + sanitized;
        source.sendFeedback(Text.literal(feedbackMessage));

        if (config.debugLogging) {
            PhasePulse.LOGGER.debug("Sent to Phase Pal: {} (speak={})", sanitized, speak);
        }

        return 1;
    }

    /**
     * Sanitizes user input by removing control characters.
     *
     * @param input The raw user input
     * @return Sanitized string safe for transmission
     */
    private static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // Remove control characters
        String sanitized = CONTROL_CHARS.matcher(input).replaceAll("");

        // Normalize multiple spaces to single space and trim
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        return sanitized;
    }

    /**
     * Shows usage help for /pal command.
     */
    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("\u00a76Phase Pal Commands:"));
        context.getSource().sendFeedback(Text.literal("\u00a77/pal chat <message> \u00a7f- Send a message to Phase Pal"));
        context.getSource().sendFeedback(Text.literal("\u00a77/pal speak <message> \u00a7f- Phase Pal speaks the response"));
        context.getSource().sendFeedback(Text.literal("\u00a78Tip: You can also use /pal <message> directly"));
        return 1;
    }
}
