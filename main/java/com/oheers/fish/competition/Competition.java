package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Competition {

    // fisher, fish length
    public static HashMap<UUID, Fish> leaderboardRegister;
    public static SortedMap<Fish, UUID> leaderboardContents;

    Bar bar;

    // (seconds) the length of the competition
    int duration;

    public Competition(int duration) {

        leaderboardRegister = new HashMap<>();
        leaderboardContents = new TreeMap<>((a, b) -> b.getLength().compareTo(a.getLength()));
        this.duration = duration;
    }

    public void start(boolean adminStart) {
        // checks skip if it's started by an admin
        if (!adminStart) {
            // if there isn't enough players on, the competition doesn't start
            if (Bukkit.getServer().getOnlinePlayers().size() < EvenMoreFish.mainConfig.getMinimumPlayers()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNotEnoughPlayers()));
                }

                return;
            }
        }

        announce();
        bar = new Bar(this.duration);
        EvenMoreFish.active = this;

        EMFCompetitionStartEvent cEvent = new EMFCompetitionStartEvent(this);
        Bukkit.getPluginManager().callEvent(cEvent);

    }

    public void end() {
        bar.end();
        EvenMoreFish.active = null;
        announceWinners();

        EMFCompetitionEndEvent cEvent = new EMFCompetitionEndEvent(this);
        Bukkit.getPluginManager().callEvent(cEvent);
    }

    public Bar getBar() {
        return bar;
    }

    private void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getCompetitionStart()));
        }
    }

    // Returns the raw leaderboard register (sorted)
    public Map<Fish, UUID> getLeaderboard() {
        return leaderboardContents;
    }

    // Returns the raw leaderboard contents (unsorted)
    public Map<UUID, Fish> getRawLeaderboardContents() {
        return leaderboardRegister;
    }

    public void runLeaderboardScan(Player fisher, Fish fish) {
        // stuff
        for (UUID holder : leaderboardRegister.keySet()) {
            if (holder.equals(fisher.getUniqueId())) {
                if (fish.getLength() > leaderboardRegister.get(holder).getLength()) {
                    leaderboardContents.remove(leaderboardRegister.get(holder));
                    leaderboardRegister.put(fisher.getUniqueId(), fish);
                    leaderboardContents.put(fish, fisher.getUniqueId());

                    return;
                }
                return;
            }
        }
        leaderboardRegister.put(fisher.getUniqueId(), fish);
        leaderboardContents.put(fish, fisher.getUniqueId());
    }

    private void announceWinners() {
        getLeaderboard(true);
    }

    public static String getLeaderboard(boolean endingCompetition) {
        StringBuilder message = new StringBuilder();
        int position = 1;

        for (Map.Entry<Fish, UUID> entry : leaderboardContents.entrySet()) {
            Fish f = entry.getKey();
            UUID p = entry.getValue();

            if (endingCompetition) giveRewards(position, p);

            // creates a limit for the number of players to be shown
            if (position > EvenMoreFish.msgs.getLeaderboardCount()) {
                break;
            }

            // It's unlikely there'll be need for placeholders in a leaderboard output. Nulled.
            message.append(new Message()
                    .setMSG(EvenMoreFish.msgs.getLeaderboard())
                    .setPlayer(Bukkit.getOfflinePlayer(p).getName())
                    .setColour(f.getRarity().getColour())
                    .setLength(f.getLength().toString())
                    .setFishCaught(f.getName())
                    .setRarity(f.getRarity().getValue())
                    .setPosition(Integer.toString(position))
                    .toString());
            message.append("\n");
            position++;
        }

        if (message.toString().equals("")) {
            // returns that there's no scores yet, if the leaderboard is empty and it's not the end of the competition
            if (!endingCompetition) return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noWinners());
            // nobody fished a fish
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getCompetitionEnd()));
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noWinners()));
                if (EvenMoreFish.msgs.noFish() != null) player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noFish()));
            }

            return null;
        } else {
            if (!endingCompetition) return message.toString();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getCompetitionEnd()));
                player.sendMessage(message.toString());
                // checks if the specific player didn't fish a fish
                if (EvenMoreFish.msgs.noFish() != null) {
                    if (!leaderboardRegister.containsKey(player.getUniqueId()))
                        player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noFish()));
                }
            }

            return null;
        }
    }

    private static void giveRewards(Integer position, UUID player) {
        // checks if it's possible to actually give the rewards
        if (Bukkit.getPlayer(player) != null) {
            // is there a reward set for the position player came?#
            if (EvenMoreFish.rewards.containsKey(position)) {
                // acts on each reward set for the player's position
                for (Reward r : EvenMoreFish.rewards.get(position)) {
                    r.run(Bukkit.getPlayer(player));
                }
            }
        }

    }
}
