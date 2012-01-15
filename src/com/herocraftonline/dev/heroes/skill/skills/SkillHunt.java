package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class SkillHunt extends ActiveSkill {

    private String huntText;

    public SkillHunt(Heroes plugin) {
        super(plugin, "Hunt");
        setDescription("Teleports you someplace within $1 blocks of the target.");
        setUsage("/skill hunt [player]");
        setArgumentRange(1, 1);
        setIdentifiers(new String[] { "skill hunt" });
        
        setTypes(SkillType.TELEPORT, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 200, false) -
                (SkillConfigManager.getUseSetting(hero, this, "radius-decrease", 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        String description = getDescription().replace("$1", radius + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.RADIUS.node(), 200);
        node.set("radius-decrease", 0);
        node.set("hunter-nearby-text", "Someone is hunting players in your area");
        return node;
    }

    @Override
    public void init() {
        super.init();
        huntText = SkillConfigManager.getUseSetting(null, this, "hunter-nearby-text", "Someone is hunting players in your area");
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            return SkillResult.INVALID_TARGET;
        }

        Location location = target.getLocation();
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 200, false) -
                (SkillConfigManager.getUseSetting(hero, this, "radius-decrease", 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        int xRadius = (int) (Math.random()*radius);
        if (Math.random() > .5) {
            xRadius = xRadius *-1;
        }
        int x = location.getBlockX() + xRadius;
        //y = 122
        int zRadius = (int) ((Math.sqrt(radius*radius - xRadius*xRadius)));
        if (Math.random() > .5) {
            zRadius = zRadius *-1;
        }
        int z = location.getBlockZ() + zRadius;
        hero.getPlayer().teleport(location.getWorld().getHighestBlockAt(x, z).getLocation());
        //hero.getPlayer().teleport(plugin.getServer().getWorld(location.getWorld().getName()).getBlockAt(x, 122, z).getLocation());
        //SafefallEffect fEffect = new SafefallEffect(this, 10000);
        //hero.addEffect(fEffect);
        List<Entity> nearbyEntities = player.getNearbyEntities(200, 128, 200);
        for (Entity e : nearbyEntities) {
            if (e instanceof Player) {
                Messaging.send((Player) e, huntText);
            }
        }
        return SkillResult.NORMAL;
    }
    /*public class SafefallEffect extends ExpirableEffect {

        public SafefallEffect(Skill skill, long duration) {
            super(skill, "Safefall", duration);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
        }

    }

    public class SkillEntityListener extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getCause() != DamageCause.FALL) {
                return;
            }

            Entity defender = event.getEntity();
            if (defender instanceof Player) {
                Player player = (Player) defender;
                Hero hero = plugin.getHeroManager().getHero(player);
                if (hero.hasEffect("Safefall")) {
                    event.setCancelled(true);
                }
            }
        }
    }*/

}