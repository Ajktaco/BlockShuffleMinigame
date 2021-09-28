package com.ajktaco.minigametest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin implements Listener
{
    HashMap<UUID, Material> playerBlock = new HashMap<>();
    List<Material> validBlocks = new ArrayList<>();
    boolean gameIsLive = false;
    int players = 0;
    int completed = 0;
    int timer = 6000;
    int realTimer = 6000;
    int checkBlockTimer;
    int timerConuter;

    @Override
    public void onEnable()
    {
        System.out.println("Blockshuffle started");
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getConfig().options().copyDefaults();
        saveDefaultConfig();

        loadBlocks();

    }

    public void loadBlocks()
    {
        List<String> configBlocks;

        configBlocks = getConfig().getStringList("Blocks");

        for(String block: configBlocks)
        {
            if(Material.getMaterial(block) != null)
            {
                validBlocks.add(Material.getMaterial(block));
            }
            else
            {
                System.out.println(block + " is not a block");
            }
        }
        System.out.println(validBlocks);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("start"))
        {
            if(!gameIsLive)
            {
                gameIsLive = true;
                startgame();
                return true;
            }
            else
            {
                sender.sendMessage("Game has already started");
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void startgame()
    {
        for(Player player: Bukkit.getOnlinePlayers())
        {
            UUID uuid = player.getUniqueId();
            playerBlock.put(uuid,randomizeBlock());
            player.sendMessage(ChatColor.AQUA + "Your block is " + playerBlock.get(uuid));
            players++;
        }
        startTimer();
        checkBlock();
    }

    public Material randomizeBlock()
    {
        Random rand = new Random();
        int arraySize = validBlocks.size();
        return validBlocks.get(rand.nextInt(arraySize));
    }

    public void checkBlock()
    {
        checkBlockTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                for(Player player: Bukkit.getOnlinePlayers())
                {
                    Material blockunder = player.getLocation().getBlock().getRelative(0, -1,0).getType();
                    if(blockunder == playerBlock.get(player.getUniqueId()))
                    {
                        player.sendMessage("Yay you stood on the right block");
                        Bukkit.broadcastMessage(player.getDisplayName() + " has stood on the correct block");
                        playerBlock.put(player.getUniqueId(), Material.NETHER_PORTAL);
                    }

                }
                checkIfDone();
            }
        },0L, 5L);
    }

    public void checkIfDone()
    {
        for(Player player: Bukkit.getOnlinePlayers())
        {
            if(playerBlock.get(player.getUniqueId()) == Material.NETHER_PORTAL)
            {
                playerBlock.put(player.getUniqueId(), null);
                completed++;
            }
        }
        if(completed == players)
        {
            Bukkit.getScheduler().cancelTask(checkBlockTimer);
            newRound();
        }
    }

    public void startTimer()
    {
        timerConuter = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                timer--;
                if(timer == 1200)
                {
                    Bukkit.broadcastMessage("1 minute remains");
                }
                else if(timer == 600)
                {
                    Bukkit.broadcastMessage("30 seconds remain");
                }
                else if(timer <= 200 && timer % 20 == 0 && timer > 0)
                {
                    Bukkit.broadcastMessage(timer/20 + " seconds remain");
                }
                else if(timer == 0)
                {
                    System.out.println(playerBlock);
                    removePlayer();
                }
            }
        },0L, 1L);
    }

    public void removePlayer()
    {
        for(Player player: Bukkit.getOnlinePlayers())
        {
            System.out.println(player.getDisplayName());
            if (playerBlock.get(player.getUniqueId()) != null)
            {
                playerBlock.remove(player.getUniqueId());
                Bukkit.broadcastMessage(ChatColor.RED + Bukkit.getPlayer(player.getUniqueId()).getDisplayName() + " was eliminated!!!");
                player.getWorld().spawnEntity(player.getLocation(), EntityType.LIGHTNING);
                players--;
            }
        }
        if(playerBlock.size() == 1)
        {
            for (UUID uuid : playerBlock.keySet())
            {
                if (playerBlock.get(uuid) == null)
                {
                    Bukkit.broadcastMessage(ChatColor.GOLD + Bukkit.getPlayer(uuid).getDisplayName() + " WON!!!!!!!");
                    resetGame();
                }
            }
        }
        else if(playerBlock.size() == 0)
        {
            Bukkit.broadcastMessage("u all lost smh");
            resetGame();
        }
        else
        {
            newRound();
        }

    }

    public void newRound()
    {
        Bukkit.getScheduler().cancelTasks(this);
        timer = realTimer;
        completed = 0;
        for(UUID uuid: playerBlock.keySet())
        {
            playerBlock.put(uuid,randomizeBlock());
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.AQUA + "Your block is " + playerBlock.get(uuid));
        }
        startTimer();
        checkBlock();
    }

    public void resetGame()
    {
        completed = 0;
        players = 0;
        timer = realTimer;
        Bukkit.getScheduler().cancelTasks(this);
        gameIsLive = false;
        playerBlock.clear();
    }

}
