/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.wynntils.core.chat.MessageType;
import com.wynntils.core.chat.RecipientType;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo
public class ChatRedirectFeature extends UserFeature {
    @Config
    public RedirectAction craftedDurability = RedirectAction.REDIRECT;

    @Config
    public RedirectAction friendJoin = RedirectAction.REDIRECT;

    @Config
    public RedirectAction heal = RedirectAction.REDIRECT;

    @Config
    public RedirectAction horse = RedirectAction.REDIRECT;

    @Config
    public RedirectAction ingredientPouch = RedirectAction.REDIRECT;

    @Config
    public RedirectAction loginAnnouncements = RedirectAction.REDIRECT;

    @Config
    public RedirectAction notEnoughMana = RedirectAction.REDIRECT;

    @Config
    public RedirectAction potion = RedirectAction.REDIRECT;

    @Config
    public RedirectAction shaman = RedirectAction.REDIRECT;

    @Config
    public RedirectAction soulPoint = RedirectAction.REDIRECT;

    @Config
    public RedirectAction speed = RedirectAction.REDIRECT;

    @Config
    public RedirectAction teleport = RedirectAction.REDIRECT;

    @Config
    public RedirectAction toolDurability = RedirectAction.REDIRECT;

    @Config
    public RedirectAction unusedPoints = RedirectAction.REDIRECT;

    private final List<Redirector> redirectors = new ArrayList<>();

    public ChatRedirectFeature() {
        register(new CraftedDurabilityRedirector());
        register(new FriendJoinRedirector());
        register(new FriendLeaveRedirector());
        register(new HealRedirector());
        register(new HealedByOtherRedirector());
        register(new HorseDespawnedRedirector());
        register(new HorseScaredRedirector());
        register(new HorseSpawnFailRedirector());
        register(new IngredientPouchSellRedirector());
        register(new LoginRedirector());
        register(new ManaDeficitRedirector());
        register(new NoTotemRedirector());
        register(new PotionsMaxRedirector());
        register(new PotionsReplacedRedirector());
        register(new SoulPointDiscarder());
        register(new SoulPointRedirector());
        register(new SpeedBoostRedirector());
        register(new TeleportationFailRedirector());
        register(new ToolDurabilityRedirector());
        register(new UnusedAbilityPointsRedirector());
        register(new UnusedSkillAndAbilityPointsRedirector());
        register(new UnusedSkillPointsRedirector());
    }

    private void register(Redirector redirector) {
        redirectors.add(redirector);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() != RecipientType.INFO) return;

        String message = e.getOriginalCodedMessage();
        MessageType messageType = e.getMessageType();

        for (Redirector redirector : redirectors) {
            RedirectAction action = redirector.getAction();
            if (action == RedirectAction.KEEP) continue;

            Matcher matcher;
            Pattern pattern = redirector.getPattern(messageType);
            // Ideally we will get rid of those "uncolored" patterns
            Pattern uncoloredPattern = redirector.getUncoloredSystemPattern();
            if (messageType == MessageType.SYSTEM && uncoloredPattern != null) {
                matcher = uncoloredPattern.matcher(ComponentUtils.stripFormatting(message));
            } else {
                if (pattern == null) continue;
                matcher = pattern.matcher(message);
            }

            if (matcher.find()) {
                e.setCanceled(true);
                if (redirector.getAction() == RedirectAction.HIDE) continue;

                for (String notification : redirector.getNotifications(matcher)) {
                    NotificationManager.queueMessage(notification);
                }
            }
        }
    }

    public enum RedirectAction {
        KEEP,
        HIDE,
        REDIRECT
    }

    public interface Redirector {
        Pattern getPattern(MessageType messageType);

        // This is a bit of a hack to support patterns without
        // color coding.
        @Deprecated
        default Pattern getUncoloredSystemPattern() {
            return null;
        }

        ChatRedirectFeature.RedirectAction getAction();

        List<String> getNotifications(Matcher matcher);
    }

    public abstract static class SimpleRedirector implements Redirector {
        @Override
        public Pattern getPattern(MessageType messageType) {
            return switch (messageType) {
                case NORMAL -> getNormalPattern();
                case BACKGROUND -> getBackgroundPattern();
                case SYSTEM -> getSystemPattern();
            };
        }

        protected Pattern getSystemPattern() {
            return null;
        }

        protected Pattern getNormalPattern() {
            return null;
        }

        protected Pattern getBackgroundPattern() {
            return null;
        }

        @Override
        public List<String> getNotifications(Matcher matcher) {
            return List.of(getNotification(matcher));
        }

        protected abstract String getNotification(Matcher matcher);
    }

    private class CraftedDurabilityRedirector extends SimpleRedirector {
        private static final Pattern UNCOLORED_SYSTEM_PATTERN = Pattern.compile(
                "^Your items are damaged and have become less effective. Bring them to a Blacksmith to repair them.$");

        @Override
        public Pattern getUncoloredSystemPattern() {
            return UNCOLORED_SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return craftedDurability;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Your items are damaged.";
        }
    }

    private class FriendJoinRedirector extends SimpleRedirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile(
                "§a(§o)?(?<name>.+)§r§2 has logged into server §r§a(?<server>.+)§r§2 as §r§aan? (?<class>.+)");
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile(
                "§r§7(§o)?(?<name>.+)§r§8(§o)? has logged into server §r§7(§o)?(?<server>.+)§r§8(§o)? as §r§7(§o)?an? (?<class>.+)");

        @Override
        protected Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        protected Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return friendJoin;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String playerName = matcher.group("name");
            String server = matcher.group("server");
            String playerClass = matcher.group("class");

            return ChatFormatting.GREEN + "→ " + ChatFormatting.DARK_GREEN
                    + playerName + " [" + ChatFormatting.GREEN
                    + server + "/" + playerClass + ChatFormatting.DARK_GREEN
                    + "]";
        }
    }

    private class FriendLeaveRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§a(?<name>.+) left the game.");
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile("§r§7(?<name>.+) left the game.");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        protected Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return friendJoin;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String playerName = matcher.group("name");

            return ChatFormatting.RED + "← " + ChatFormatting.DARK_GREEN + playerName;
        }
    }

    private class HealRedirector extends SimpleRedirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile("^§r§c\\[\\+(\\d+) ❤\\]$");

        @Override
        protected Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return heal;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String amount = matcher.group(1);

            return ChatFormatting.DARK_RED + "[+" + amount + " ❤]";
        }
    }

    private class HealedByOtherRedirector extends SimpleRedirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile("^.+ gave you §r§c\\[\\+(\\d+) ❤\\]$");
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile("^.+ gave you §r§7§o\\[\\+(\\d+) ❤\\]$");

        @Override
        protected Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        protected Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return heal;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String amount = matcher.group(1);

            return ChatFormatting.DARK_RED + "[+" + amount + " ❤]";
        }
    }

    private class HorseDespawnedRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("§dSince you interacted with your inventory, your horse has despawned.");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return horse;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_PURPLE + "Your horse has despawned.";
        }
    }

    private class HorseScaredRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("§dYour horse is scared to come out right now, too many mobs are nearby.");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return horse;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Nearby mobs prevent horse spawning!";
        }
    }

    private class HorseSpawnFailRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§4There is no room for a horse.");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return horse;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "No room for a horse!";
        }
    }

    private class IngredientPouchSellRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("§dYou have sold §r§7(.+)§r§d ingredients for a total of §r§a(.+)§r§d\\.$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return ingredientPouch;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            Integer ingredientCount = Integer.parseInt(matcher.group(1));
            String ingredientString = ingredientCount.toString() + " §dingredient" + (ingredientCount == 1 ? "" : "s");

            String emeraldString = matcher.group(2);

            String formattedOverlayString = String.format("§dSold §7%s §dfor §a%s§d.", ingredientString, emeraldString);

            return formattedOverlayString;
        }
    }

    private class LoginRedirector extends SimpleRedirector {
        private static final Pattern NORMAL_PATTERN =
                Pattern.compile("^§.\\[§r§.([A-Z+]+)§r§.\\] §r§.(.*)§r§. has just logged in!$");
        private static final Pattern BACKGROUND_PATTERN =
                Pattern.compile("^(?:§r§8)?\\[§r§7([A-Z+]+)§r§8\\] §r§7(.*)§r§8 has just logged in!$");

        @Override
        protected Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        protected Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return loginAnnouncements;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String rank = matcher.group(1);
            String playerName = matcher.group(2);

            return ChatFormatting.GREEN + "→ " + WynnPlayerUtils.getFormattedRank(rank) + playerName;
        }
    }

    private class ManaDeficitRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("^§4You don't have enough mana to cast that spell!$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return notEnoughMana;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Not enough mana to do that spell!";
        }
    }

    private class NoTotemRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§4You have no active totems near you$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return shaman;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "No totems nearby!";
        }
    }

    private class PotionsMaxRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("§4You already are holding the maximum amount of potions allowed.");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return potion;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "At potion charge limit!";
        }
    }

    private class PotionsReplacedRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("§7One less powerful potion was replaced to open space for the added one.");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return potion;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.GRAY + "Lesser potion replaced.";
        }
    }

    private class TeleportationFailRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§cThere are aggressive mobs nearby...$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return teleport;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Nearby mobs prevent scroll teleportation!";
        }
    }

    private class SoulPointDiscarder implements Redirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("^§5As the sun rises, you feel a little bit safer...$");
        private static final Pattern BACKGROUND_PATTERN =
                Pattern.compile("^(§r§8)?As the sun rises, you feel a little bit safer...$");

        @Override
        public Pattern getPattern(MessageType messageType) {
            return switch (messageType) {
                case BACKGROUND -> BACKGROUND_PATTERN;
                case SYSTEM -> SYSTEM_PATTERN;
                default -> null;
            };
        }

        @Override
        public RedirectAction getAction() {
            return soulPoint;
        }

        @Override
        public List<String> getNotifications(Matcher matcher) {
            // Soul point messages comes in two lines. We just throw away the chatty one
            // if we have hide or redirect as action.
            return List.of();
        }
    }

    private class SoulPointRedirector extends SimpleRedirector {
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile("^§r§7\\[(\\+\\d+ Soul Points?)\\]$");
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("^§d\\[(\\+\\d+ Soul Points?)\\]$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        protected Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return soulPoint;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            // Send the matching part, which could be +1 Soul Point or +2 Soul Points, etc.
            return ChatFormatting.LIGHT_PURPLE + matcher.group(1);
        }
    }

    private class SpeedBoostRedirector extends SimpleRedirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile("^\\+3 minutes speed boost.$");

        @Override
        protected Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return speed;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.AQUA + "+3 minutes" + ChatFormatting.GRAY + " speed boost";
        }
    }

    private class ToolDurabilityRedirector extends SimpleRedirector {
        private static final Pattern UNCOLORED_SYSTEM_PATTERN = Pattern.compile(
                "^Your tool has 0 durability left! You will not receive any new resources until you repair it at a Blacksmith.$");

        @Override
        public Pattern getUncoloredSystemPattern() {
            return UNCOLORED_SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return toolDurability;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Your tool has 0 durability!";
        }
    }

    private class UnusedAbilityPointsRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile(
                "^§4You have §r§b§l(\\d+) unused Ability Points?! §r§4Right-Click while holding your compass to use them$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return unusedPoints;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String unusedAbilityPoints = matcher.group(1);

            return getUnusedAbilityPointsMessage(unusedAbilityPoints);
        }

        public static String getUnusedAbilityPointsMessage(String unusedAbilityPoints) {
            return ChatFormatting.DARK_AQUA + "You have " + ChatFormatting.BOLD + unusedAbilityPoints
                    + ChatFormatting.RESET + ChatFormatting.DARK_AQUA + " unused ability points";
        }
    }

    private class UnusedSkillAndAbilityPointsRedirector implements Redirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile(
                "^§4You have §r§c§l(\\d+) unused Skill Points?§r§4 and §r§b§l(\\d+) unused Ability Points?! §r§4Right-Click while holding your compass to use them$");

        @Override
        public Pattern getPattern(MessageType messageType) {
            if (messageType == MessageType.SYSTEM) {
                return SYSTEM_PATTERN;
            } else {
                return null;
            }
        }

        @Override
        public RedirectAction getAction() {
            return unusedPoints;
        }

        @Override
        public List<String> getNotifications(Matcher matcher) {
            String unusedSkillPoints = matcher.group(1);
            String unusedAbilityPoints = matcher.group(2);

            return List.of(
                    UnusedSkillPointsRedirector.getUnusedSkillPointsMessage(unusedSkillPoints),
                    UnusedAbilityPointsRedirector.getUnusedAbilityPointsMessage(unusedAbilityPoints));
        }
    }

    private class UnusedSkillPointsRedirector extends SimpleRedirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile(
                "^§4You have §r§c§l(\\d+) unused Skill Points?! §r§4Right-Click while holding your compass to use them$");

        @Override
        protected Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        public RedirectAction getAction() {
            return unusedPoints;
        }

        @Override
        protected String getNotification(Matcher matcher) {
            String unusedSkillPoints = matcher.group(1);

            return getUnusedSkillPointsMessage(unusedSkillPoints);
        }

        public static String getUnusedSkillPointsMessage(String unusedSkillPoints) {
            return ChatFormatting.DARK_RED + "You have " + ChatFormatting.BOLD + unusedSkillPoints
                    + ChatFormatting.RESET + ChatFormatting.DARK_RED + " unused skill points";
        }
    }
}
