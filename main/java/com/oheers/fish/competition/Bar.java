package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class Bar {

    String title;
    BossBar bar;

    int timeLeft;
    int totalTime;

    Ticker ticker;

    public Bar(int totalTime) {
        this.totalTime = totalTime;
        // adding an offset so it doesn't instantly start counting down
        this.timeLeft = totalTime+1;

        createBar();
        renderBars();
        begin();
    }

    public boolean timerUpdate() {
        if (checkEnd()) {
            timeLeft--;
            setTitle();
            setProgress();
            return true;
        } else {
            return false;
        }
    }

    private void begin() {
        this.ticker = new Ticker(this);
        ticker.runTaskTimer(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), 0, 20);
    }

    public void end() {
        removeAllPlayers();
        this.ticker.cancel();
    }

    private void setProgress() {
        double progress = (double) (timeLeft) / (double) (totalTime);

        if (progress < 0) {
            bar.setProgress(0.0D);
        } else if (progress > 1) {
            bar.setProgress(1.0D);
        } else {
            bar.setProgress(progress);
        }

    }

    private void setTitle() {
        bar.setTitle(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getBarPrefix()) + ChatColor.RESET + FishUtils.timeFormat(timeLeft) + EvenMoreFish.msgs.getRemainingWord());
    }

    private void show() {
        bar.setVisible(true);
    }

    private void hide() {
        bar.setVisible(false);
    }

    public void createBar() {
        BarColor bC = BarColor.valueOf(EvenMoreFish.mainConfig.getBossbarColour());
        if (bC == null) bC = BarColor.GREEN;

        bar = Bukkit.getServer().createBossBar(title, bC, BarStyle.SEGMENTED_10);
    }

    // Shows the bar to all players online
    private void renderBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        show();
    }

    // Checks if there's 0 seconds left on the timer
    private boolean checkEnd() {
        if (timeLeft == 1) {
            hide();
            return false;
        } else {
            return true;
        }
    }

    public void addPlayer(Player player) {
        bar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        bar.removePlayer(player);
    }

    public void removeAllPlayers() {
        bar.removeAll();
    }
}
