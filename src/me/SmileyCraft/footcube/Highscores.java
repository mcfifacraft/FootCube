
package me.SmileyCraft.footcube;

import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import org.bukkit.scheduler.BukkitScheduler;

public class Highscores
{
    private Organization organization;
    private FootCube plugin;
    private int taskID;
    private BukkitScheduler bs;
    private long lastUpdate;
    private double[] bestRatings;
    private int[] mostGoals;
    private int[] mostWins;
    private int[] longestStreak;
    private int lastUpdatedParticipent;
    private String[] participents;
    private String[] recordHolders;
    public boolean isUpdating;
    private ArrayList<Player> waitingPlayers;
    
    public Highscores(final Organization org, final FootCube pl, final Player p) {
        this.waitingPlayers = new ArrayList<Player>();
        this.organization = org;
        this.plugin = pl;
        this.bs = this.plugin.getServer().getScheduler();
        this.update(p);
    }
    
    public boolean needsUpdate() {
        return this.lastUpdate + 600000L < System.currentTimeMillis();
    }
    
    public void addWaitingPlayer(final Player p) {
        this.waitingPlayers.add(p);
        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Scores are updating, please wait");
        p.sendMessage(ChatColor.GOLD + "Updating 500 players per second with " + (this.participents.length - this.lastUpdatedParticipent) + " players to go");
    }
    
    public void showHighscores(final Player p) {
        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GOLD + "All FootCube highscores:");
        p.sendMessage(ChatColor.AQUA + "Best ratings:");
        for (int i = 0; i < 3; ++i) {
            p.sendMessage(new StringBuilder().append(ChatColor.GRAY).append(i + 1).append(". ").append(this.recordHolders[i]).append(" - ").append(this.bestRatings[i]).toString());
        }
        p.sendMessage(ChatColor.AQUA + "Most goals:");
        for (int i = 0; i < 3; ++i) {
            p.sendMessage(new StringBuilder().append(ChatColor.GRAY).append(i + 1).append(". ").append(this.recordHolders[i + 3]).append(" - ").append(this.mostGoals[i]).toString());
        }
        p.sendMessage(ChatColor.AQUA + "Most wins:");
        for (int i = 0; i < 3; ++i) {
            p.sendMessage(new StringBuilder().append(ChatColor.GRAY).append(i + 1).append(". ").append(this.recordHolders[i + 6]).append(" - ").append(this.mostWins[i]).toString());
        }
        p.sendMessage(ChatColor.AQUA + "Longest win streak:");
        for (int i = 0; i < 3; ++i) {
            p.sendMessage(new StringBuilder().append(ChatColor.GRAY).append(i + 1).append(". ").append(this.recordHolders[i + 9]).append(" - ").append(this.longestStreak[i]).toString());
        }
    }
    
    public void update(final Player p) {
        (this.waitingPlayers = new ArrayList<Player>()).add(p);
        this.lastUpdate = System.currentTimeMillis();
        this.isUpdating = true;
        this.bestRatings = new double[] { 0.0, 0.0, 0.0 };
        this.mostGoals = new int[3];
        this.mostWins = new int[3];
        this.longestStreak = new int[3];
        this.recordHolders = new String[] { "nobody", "nobody", "nobody", "nobody", "nobody", "nobody", "nobody", "nobody", "nobody", "nobody", "nobody", "nobody" };
        this.participents = new String[this.organization.matches.keySet().size()];
        int i = 0;
        for (final String s : this.organization.matches.keySet()) {
            this.participents[i] = s;
            ++i;
        }
        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Scores are updating, please wait");
        p.sendMessage(ChatColor.GOLD + "Updating 500 players per second with " + this.participents.length + " players to go");
        this.lastUpdatedParticipent = 0;
        this.taskID = this.bs.scheduleAsyncRepeatingTask((Plugin)this.plugin, (Runnable)new Runnable() {
            public void run() {
                Highscores.this.continueUpdate();
            }
        }, 1L, 1L);
    }
    
    private void continueUpdate() {
        for (int x = this.lastUpdatedParticipent; x < this.lastUpdatedParticipent + 25; ++x) {
            if (this.participents.length == 0) {
                this.plugin.getServer().getScheduler().cancelTask(this.taskID);
                this.isUpdating = false;
                for (final Player p : this.waitingPlayers) {
                    this.showHighscores(p);
                }
                break;
            }
            final String s = this.participents[x];
            final String name = this.organization.uuidConverter.get(s);
            final int m = this.organization.matches.get(s);
            final int w = this.organization.wins.get(s);
            final int t = this.organization.ties.get(s);
            final int g = this.organization.goals.get(s);
            final int b = this.organization.bestWinStreak.get(s);
            final double multiplier = 1.0 - Math.pow(0.9, m);
            double goalBonus = 0.5;
            if (m > 0) {
                goalBonus = 1.0 - multiplier * Math.pow(0.2, g / (double)m) - 0.5 / Math.pow(1.1111111111111112, m);
            }
            double addition = 0.0;
            if (m > 0 && w + t > 0) {
                addition = 8.0 * (1.0 / (100 * m / (w + 0.5 * t) / 100.0)) - 4.0;
            }
            else if (m > 0) {
                addition = -4.0;
            }
            final double skillLevel = (int)(100.0 * (5.0 + goalBonus + addition * multiplier)) / 100.0;
            for (int i = 0; i < 3; ++i) {
                if (skillLevel >= this.bestRatings[i]) {
                    for (int j = 2; j > i; --j) {
                        this.bestRatings[j] = this.bestRatings[j - 1];
                        this.recordHolders[j] = this.recordHolders[j - 1];
                    }
                    this.bestRatings[i] = skillLevel;
                    this.recordHolders[i] = name;
                    break;
                }
            }
            for (int i = 0; i < 3; ++i) {
                if (g >= this.mostGoals[i]) {
                    for (int j = 2; j > i; --j) {
                        this.mostGoals[j] = this.mostGoals[j - 1];
                        this.recordHolders[j + 3] = this.recordHolders[j + 2];
                    }
                    this.mostGoals[i] = g;
                    this.recordHolders[i + 3] = name;
                    break;
                }
            }
            for (int i = 0; i < 3; ++i) {
                if (w >= this.mostWins[i]) {
                    for (int j = 2; j > i; --j) {
                        this.mostWins[j] = this.mostWins[j - 1];
                        this.recordHolders[j + 6] = this.recordHolders[j + 5];
                    }
                    this.mostWins[i] = w;
                    this.recordHolders[i + 6] = name;
                    break;
                }
            }
            for (int i = 0; i < 3; ++i) {
                if (b >= this.longestStreak[i]) {
                    for (int j = 2; j > i; --j) {
                        this.longestStreak[j] = this.longestStreak[j - 1];
                        this.recordHolders[j + 9] = this.recordHolders[j + 8];
                    }
                    this.longestStreak[i] = b;
                    this.recordHolders[i + 9] = name;
                    break;
                }
            }
            if (x >= this.participents.length - 1) {
                this.plugin.getServer().getScheduler().cancelTask(this.taskID);
                this.isUpdating = false;
                for (final Player p2 : this.waitingPlayers) {
                    this.showHighscores(p2);
                }
                break;
            }
        }
        this.lastUpdatedParticipent += 25;
        this.lastUpdate = System.currentTimeMillis();
    }
}
