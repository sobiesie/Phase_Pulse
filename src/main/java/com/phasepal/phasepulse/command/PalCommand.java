package com.phasepal.phasepulse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

/**
 * Handles the /pal command for sending messages to Phase Pal.
 *
 * Commands:
 * - /pal <message> - Send a chat message to Phase Pal
 * - /pal speak <message> - Send a message for Phase Pal to speak aloud
 *
 * Security: Only captures messages from the local player who explicitly
 * types the command. Does not capture public chat or other players' messages.
 */
public class PalCommand {

    private static final String EVENT_CHAT_MESSAGE = "chat_message";

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

        // Validate message
        if (message == null || message.trim().isEmpty()) {
            context.getSource().sendError(Text.literal("Message cannot be empty"));
            return 0;
        }

        String trimmedMessage = message.trim();

        // Check message length (prevent abuse)
        if (trimmedMessage.length() > 500) {
            context.getSource().sendError(Text.literal("Message too long (max 500 characters)"));
            return 0;
        }

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
     * Shows usage help for /pal command.
     */
    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("§6Phase Pal Commands:"));
        context.getSource().sendFeedback(Text.literal("§7/pal <message> §f- Send a message to Phase Pal"));
        context.getSource().sendFeedback(Text.literal("§7/pal speak <message> §f- Phase Pal speaks the response"));
        return 1;
    }
}
