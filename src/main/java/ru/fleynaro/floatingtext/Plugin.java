/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.fleynaro.floatingtext;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author user
 */
public class Plugin extends PluginBase implements Listener {
    
    private Level level;
    private int addCount = 0;
    private Config cfg;
    private final Map<Vector3, FloatingTextParticle> texts = new HashMap<>();
    
    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        this.saveDefaultConfig();
        this.loadConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.level = getServer().getDefaultLevel();
        
        this.getLogger().info(TextFormat.WHITE +"The plugin "+ TextFormat.RED +"3D Floating Text"+ TextFormat.WHITE +" has been loaded.");
    }
    
    private void loadConfig() {
        cfg = this.getConfig();
        
        int i = 0;
        while ( true ) {
            String paramName = "texts.text"+ (i + 1);
            String strPos = cfg.getString(paramName +".pos", "not");
            if ( strPos.equals("not") ) break;
            
            double[] pos = new double[3];
            int j = 0;
            for (String coord : strPos.split(",", 3)) {
                pos[j ++] = Float.parseFloat(coord);
            }
            if ( pos[1] > 255 ) {
                pos[1] = (double)this.level.getHighestBlockAt((int)pos[0], (int)pos[2]) + 2;
            }
            
            this.createFloatingText(new Vector3(pos[0], pos[1], pos[2]), "", cfg.getString(paramName +".text", "Not defined message"));
            i ++;
        }
    }
    
    private FloatingTextParticle createFloatingText(Vector3 pos, String title, String text) {
        FloatingTextParticle newFtext = new FloatingTextParticle(pos, title, TextFormat.colorize(text));
        texts.put(pos, newFtext);
        return newFtext;
    }
    
    private void addAllText(Player player) {
        for( FloatingTextParticle text : texts.values() ) {
            this.level.addParticle(text, player);
        }
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        addAllText(event.getPlayer());
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerExit(PlayerJoinEvent event) {
        //...
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        
        if ( player != null ) {
            if( command.getName().toLowerCase().equals("ftext") && player.hasPermission("floatingtext.create") ) {
                Vector3 pos = player.getPosition();
                int textid = texts.size() + 1;
                String text = args.length > 0 ? args[0] : "Your text";

                this.level.addParticle(this.createFloatingText(pos, "", "Text"+ textid +"\n" + text), player);
                cfg.getSection("texts").put("text" + textid, new ConfigSection(new LinkedHashMap<String, Object>() {{
                    put("pos", pos.getX() +","+ pos.getY() +","+ pos.getZ());
                    put("text", text);
                }}));
                cfg.save();
                
                player.sendMessage("The floating text"+ textid +" has been created!");
            }
        }
        return true;
    }
}
