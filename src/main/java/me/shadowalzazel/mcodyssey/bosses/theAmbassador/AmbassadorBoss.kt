package me.shadowalzazel.mcodyssey.bosses.theAmbassador

import me.shadowalzazel.mcodyssey.bosses.utility.OdysseyBoss
import me.shadowalzazel.mcodyssey.odysseyUtility.*
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Illusioner
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

// DO COROUTINES LATER !!!

@Suppress("DEPRECATION")
class AmbassadorBoss : OdysseyBoss("The Ambassador", "Illusioner") {

    // Boss Spawning Logic
    var bossEntity: Illusioner? = null
    var despawnTimer = System.currentTimeMillis()
    // Trading Mechanic
    var patience: Double = 75.0
    var appeasement: Double = 0.0
    private var playerLikeness = mutableMapOf<UUID, Double>()
    private var playersGiftCooldown = mutableMapOf<UUID, Long>()
    private val itemLootTable = listOf(Material.NETHERITE_INGOT, Material.DIAMOND, Material.EMERALD, Material.AMETHYST_SHARD, Material.ENDER_CHEST,
        Material.COPPER_INGOT, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.GOAT_HORN, Material.PAINTING,
        Material.ENCHANTED_BOOK, Material.SHULKER_BOX, Material.ENCHANTED_GOLDEN_APPLE, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.ORANGE_TULIP,
        Material.RED_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.SUNFLOWER, Material.BOOK, Material.ROTTEN_FLESH,
        Material.LILAC, Material.ROSE_BUSH, Material.PEONY, Material.ACACIA_LOG, Material.OAK_LOG, Material.SPRUCE_LOG, Material.DARK_OAK_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.MANGROVE_LOG,
        Material.ACACIA_WOOD, Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.DARK_OAK_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD, Material.MANGROVE_WOOD, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.COPPER_BLOCK)
    private val giftLootTable = mapOf("RefinedNeptunianDiamonds" to RefinedNeptunianDiamonds(), "RefinedIojovianEmeralds" to RefinedIojovianEmeralds(), "ArtificialStarUnit" to ArtificialStarUnit(),
        "IdescineSaplings" to IdescineSaplings(), "PolymorphicGlue" to PolymorphicGlue(), "HawkingEntangledUnit" to HawkingEntangledUnit(), "KugelblitzContainmentSilo" to KugelblitzContainmentSilo(),
        "NeutroniumBarkScraps" to NeutroniumBarkScraps(), "GalvanizedSteel" to GalvanizedSteel(), "PureAlloyCopper" to PureAlloyCopper())
    // Combat Mechanic
    private var takeDamageCooldown: Long = 0L
    var specialAttacksCooldown = mutableMapOf<String, Long>()
    // Quotes MOVE HERE
    private val randomMessageList = listOf("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Such Folly, Such Weakness!", "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}I can not tolerate such incompetence",
        "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}The damage you think your inflicting is minimal!", "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}I am just disappointed...",
        "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Just stop... Your attacks are worthless!")


    // Create Kinetic Weapon
    private fun createAmbassadorWeapon(): ItemStack {
        val kineticBlaster = ItemStack(Material.BOW, 1)

        // Add lore and name
        val kineticBlasterMeta: ItemMeta = kineticBlaster.itemMeta
        kineticBlasterMeta.setDisplayName("${ChatColor.LIGHT_PURPLE}Kinetic Blaster")
        val kineticBlasterLore = listOf("A weapon commissioned to shoot kinetic darts", "Designed by the current natives")
        kineticBlasterMeta.lore = kineticBlasterLore

        // Add Enchantments
        kineticBlasterMeta.addEnchant(Enchantment.ARROW_DAMAGE, 5, true)
        kineticBlasterMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true)
        kineticBlasterMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true)
        kineticBlasterMeta.addEnchant(Enchantment.DURABILITY, 3, true)

        // Create weapon
        kineticBlaster.itemMeta = kineticBlasterMeta
        return kineticBlaster
    }


    // Spawn boss near players
    private fun spawnBoss(odysseyWorld: World): Illusioner {
        val worldPlayers = odysseyWorld.players
        for (somePlayer in worldPlayers) {
            somePlayer.sendMessage("${ChatColor.GOLD}${ChatColor.MAGIC}[Vail]${ChatColor.RESET}${ChatColor.YELLOW} My Ambassador has arrived!")
            somePlayer.playSound(somePlayer.location, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 2.5F, 0.9F)
        }
        val spawningPlayer = worldPlayers.random()
        val spawningLocation = spawningPlayer.location
        spawningLocation.x += (-100..100).random()
        spawningLocation.z += (-100..100).random()
        spawningLocation.y = 250.0
        println("The Ambassador has arrived at ${spawningLocation.x}, ${spawningLocation.z}")
        return odysseyWorld.spawnEntity(spawningLocation, EntityType.ILLUSIONER) as Illusioner
    }


    // Create Ambassador Boss Entity
    fun createBoss(odysseyWorld: World) {
        val ambassadorEntity: Illusioner = spawnBoss(odysseyWorld)
        // 1200 tks = 60 sec
        // Add Potion Effects
        val voidFall = PotionEffect(PotionEffectType.SLOW_FALLING, 1200, 1)
        val voidGlow = PotionEffect(PotionEffectType.GLOWING, 1200, 1)
        val voidSolar = PotionEffect(PotionEffectType.FIRE_RESISTANCE, 99999, 3)
        val enhancedHealth = PotionEffect(PotionEffectType.HEALTH_BOOST, 99999, 235)
        val ankiRainEffects = listOf(enhancedHealth, voidFall, voidSolar, voidGlow)
        ambassadorEntity.addPotionEffects(ankiRainEffects)

        // Change Default Behaviour
        ambassadorEntity.customName = "${ChatColor.LIGHT_PURPLE}$bossName"
        ambassadorEntity.isCustomNameVisible = true
        ambassadorEntity.removeWhenFarAway = false
        ambassadorEntity.isCanJoinRaid = false
        ambassadorEntity.isAware = false
        ambassadorEntity.canPickupItems = true
        ambassadorEntity.health = 950.0

        // Add Item
        val ambassadorWeapon: ItemStack = createAmbassadorWeapon()
        ambassadorEntity.clearActiveItem()
        ambassadorEntity.equipment.setItemInMainHand(ambassadorWeapon)

        // Change boss class
        bossEntity = ambassadorEntity
    }


    // Defeat Boss
    fun defeatedBoss(ambassadorEntity: Illusioner, vanquisher: Player) {
        val nearbyPlayers = ambassadorEntity.world.getNearbyPlayers(ambassadorEntity.location, 64.0)
        for (somePlayer in ambassadorEntity.world.players) {
            somePlayer.sendMessage("${ChatColor.YELLOW}${ChatColor.ITALIC}The Ambassador has departed ungracefully!")
            somePlayer.sendMessage("${ChatColor.YELLOW}${ChatColor.ITALIC}With ${ChatColor.GOLD}${vanquisher.name} ${ChatColor.RESET}${ChatColor.YELLOW}${ChatColor.ITALIC}taking the final blow!")
            somePlayer.playSound(somePlayer, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F)
            if (somePlayer in nearbyPlayers) {
                somePlayer.giveExp(75000)
            }
        }
    }

    // activate boss
    private fun activateBoss(someTarget: Entity) {
        bossEntity!!.isAware = true
        for (somePlayer in someTarget.world.players) {
            somePlayer.playSound(somePlayer, Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.8F)
        }

    }


    // Create a new firework entity
    private fun createSuperFirework(targetPlayer: Player): Firework {
        // MAYBE SHOOT FROM SPAWNED AMBASSADOR
        val superFirework: Firework = targetPlayer.world.spawnEntity(bossEntity!!.location, EntityType.FIREWORK) as Firework
        val randomColors = listOf(Color.BLUE, Color.RED, Color.YELLOW, Color.FUCHSIA, Color.AQUA)
        val superFireworkMeta = superFirework.fireworkMeta

        // Add Effects and Power
        superFireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(randomColors.random()).withFade(randomColors.random()).trail(true).flicker(true).build())
        superFireworkMeta.power = 120
        // FIX VELOCITY
        superFirework.velocity = targetPlayer.location.direction.subtract(bossEntity!!.location.direction)
        superFirework.ticksToDetonate = 8
        return superFirework
    }


    // Do Firework damage
    private fun doFireworkDamage(somePlayers: MutableCollection<Player>) {
        // Create sounds, particles, and do damage to nearby players
        for (somePlayer in somePlayers) {
            // IDK TO SPAWN AT PLAYER WITH 0 VEL OR WHAT
            createSuperFirework(somePlayer)
            somePlayer.damage(17.5)
            somePlayer.playSound(somePlayer.location, Sound.AMBIENT_BASALT_DELTAS_MOOD, 2.5F, 0.8F)
            somePlayer.playSound(somePlayer.location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.5F, 0.8F)
            somePlayer.playSound(somePlayer.location, Sound.ENTITY_IRON_GOLEM_DEATH, 2.0F, 0.8F)
            somePlayer.world.spawnParticle(Particle.FLASH, somePlayer.location, 5, 1.0, 1.0, 1.0)
            somePlayer.world.spawnParticle(Particle.LAVA, somePlayer.location, 35, 1.5, 1.0, 1.5)
        }
    }


    // Firework Attack Call
    private fun fireworkAttack(targetPlayer: Player) {
        // Find players for splash
        val playersNearTarget = targetPlayer.world.getNearbyPlayers(targetPlayer.location, 3.5)
        doFireworkDamage(playersNearTarget)
    }


    // Spawn a dummy clone
    private fun spawnDummy() {
        // Find Location
        val currentLocation = bossEntity!!.location
        val randomXZLocation = (-5..5).random()
        currentLocation.x += randomXZLocation
        currentLocation.z += randomXZLocation
        currentLocation.y += 3

        // Create Dummy
        val ambassadorDummy = bossEntity!!.world.spawnEntity(currentLocation, EntityType.ILLUSIONER) as Illusioner
        ambassadorDummy.customName = "${ChatColor.LIGHT_PURPLE}$bossName"
        ambassadorDummy.isCustomNameVisible = true
        ambassadorDummy.isCanJoinRaid = false
        ambassadorDummy.isAware = true
        ambassadorDummy.lootTable = null
        ambassadorDummy.health = 25.0

        // Add Item
        val ambassadorWeapon: ItemStack = createAmbassadorWeapon()
        ambassadorDummy.clearActiveItem()
        ambassadorDummy.equipment.setItemInMainHand(ambassadorWeapon)

    }


    // Do Gravity Wave Damage
    private fun doGravityWaveDamage(somePlayers: MutableCollection<Player>, someWorld: World) {
        val gravityRise = PotionEffect(PotionEffectType.LEVITATION, 150, 0)
        val gravityShatter = PotionEffect(PotionEffectType.WEAKNESS, 200, 1)
        val gravityWaveEffects = listOf(gravityRise, gravityShatter)

        for (somePlayer in somePlayers) {
            // Do damage, apply effects and teleport
            somePlayer.addPotionEffects(gravityWaveEffects)
            somePlayer.damage(18.5)
            val gravityPush = somePlayer.location
            gravityPush.y += 2.5
            somePlayer.teleport(gravityPush)

            // Apply sound and particles
            somePlayer.playSound(somePlayer, Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1.3F, 1.1F)
            somePlayer.playSound(somePlayer, Sound.ITEM_TRIDENT_THUNDER, 2.5F, 0.8F)
            somePlayer.playSound(somePlayer, Sound.BLOCK_ANVIL_LAND, 1.0F, 0.5F)
            somePlayer.playSound(somePlayer, Sound.BLOCK_ANVIL_BREAK, 1.0F, 0.8F)
            somePlayer.playSound(somePlayer, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0F, 1.0F)
            somePlayer.playSound(somePlayer, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.5F, 1.0F)
            someWorld.spawnParticle(Particle.DAMAGE_INDICATOR, somePlayer.location, 15, 1.5, 0.5, 1.5)
            someWorld.spawnParticle(Particle.CRIT, somePlayer.location, 15, 2.5, 0.5, 2.5)
            someWorld.spawnParticle(Particle.END_ROD, somePlayer.location, 15, 2.0, 1.0, 2.0)
            someWorld.spawnParticle(Particle.FLASH, somePlayer.location, 5, 1.0, 1.0, 1.0)
            someWorld.spawnParticle(Particle.EXPLOSION_NORMAL, somePlayer.location, 10, 2.0, 1.0, 2.0)
        }
    }


    // Gravity Wave Attack Call
    private fun gravityWaveAttack(targetPlayer: Entity) {
        // Find Players Near Attacker
        val playersNearTarget = targetPlayer.world.getNearbyPlayers(targetPlayer.location, 9.5)
        doGravityWaveDamage(playersNearTarget, targetPlayer.world)
        // Spawn Dummies
        spawnDummy()
        spawnDummy()
    }


    // Pull player and do damage
    fun voidPullBackAttack(targetPlayer: Player) {
        val voidSlow = PotionEffect(PotionEffectType.SLOW, 200, 0)
        val voidRise = PotionEffect(PotionEffectType.LEVITATION, 200, 0)
        val voidPullEffects = listOf(voidRise, voidSlow)

        // Teleport and damage
        targetPlayer.teleport(bossEntity!!.location)
        targetPlayer.addPotionEffects(voidPullEffects)
        targetPlayer.damage(5.5)

        // Play sounds and particles
        targetPlayer.playSound(targetPlayer, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8F, 1.0F)
        targetPlayer.playSound(targetPlayer, Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1.0F, 1.1F)
        targetPlayer.playSound(targetPlayer, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.8F, 0.9F)
        targetPlayer.world.spawnParticle(Particle.DAMAGE_INDICATOR, targetPlayer.location, 5, 1.5, 0.5, 1.5)
        targetPlayer.world.spawnParticle(Particle.VIBRATION, targetPlayer.location, 15, 2.5, 0.5, 2.5)
        targetPlayer.world.spawnParticle(Particle.END_ROD, targetPlayer.location, 15, 2.0, 1.0, 2.0)
    }


    // Attack damager
    private fun attackToDamager(someDamager: Entity) {
        // Quotes
        val timeElapsed: Long = System.currentTimeMillis() - takeDamageCooldown
        if (timeElapsed >= 7000) {
            if (someDamager is Player) {
                when((0..4).random()) {
                    2, 3, 4 -> {
                        gravityWaveAttack(someDamager)
                    }
                    0, 1 -> {
                        fireworkAttack(someDamager)
                    }
                }
            }
            else {
                gravityWaveAttack(bossEntity as Entity)
            }
            val randomAttackQuote = randomMessageList.random()
            takeDamageCooldown = System.currentTimeMillis()
            for (somePlayer in bossEntity!!.world.getNearbyPlayers(bossEntity!!.location, 17.5)) {
                somePlayer.sendMessage(randomAttackQuote)
            }
        }
    }


    // Check damage source and current stats
    fun detectDamage(someDamager: Entity, someDamage: Double) {
        // Quotes MOVE LATER
        val criticalMoodQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}${someDamager.name} wants to endanger you all!"
        val patiencePatienceQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}${someDamager.name} is testing my patience!"
        val disrespectQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}${someDamager.name} has shown me extreme disrespect!"
        val disappointedQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}${someDamager.name}... I expected more from you..."
        val dislikeQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}${someDamager.name}... Does your honor not mean anything?!"
        val firstBadContactQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}${someDamager.name}, that is not an appropriate way to introduce yourself, though what is expected from such a lowlife..."
        val activationQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Due to ${someDamager.name}'s insolence, You all shall be taught some respect!"
        val whoActivationQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Time to teach these lowlifes basic manners..."
        val patienceBadMoodQuote = "${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Who is doing that?!"

        // Check if Patience
        if (patience > 0) {
            var messageQuote: String?

            // Messages if player damage
            if (someDamager is Player) {
                // Add likeness and bad first contact
                if (!playerLikeness.containsKey(someDamager.uniqueId)) {
                    playerLikeness[someDamager.uniqueId] = 0.0
                    messageQuote = firstBadContactQuote
                }
                // Check if likability is high
                else {
                    messageQuote = if (playerLikeness[someDamager.uniqueId]!! >= 65.0) {
                        disappointedQuote
                    } else if (playerLikeness[someDamager.uniqueId]!! >= 25.0) {
                        dislikeQuote
                    } else {
                        patiencePatienceQuote
                    }
                    playerLikeness[someDamager.uniqueId] = playerLikeness[someDamager.uniqueId]!! - someDamage
                }
                // Change his mood
                appeasement -= (someDamage + 2.0)
                patience -= someDamage
                // Check if critical activation
                if (patience <= 0) {
                    // Check if activated
                    messageQuote = activationQuote
                    activateBoss(someDamager)
                }
                else if (patience <= 10) {
                    // Check if near criticality
                    messageQuote = criticalMoodQuote
                }
                else if (appeasement <= -5) {
                    // Check if low appeasement
                    messageQuote = disrespectQuote
                }
            }
            // Entity damages
            else {
                messageQuote = patienceBadMoodQuote
                // Change his mood
                appeasement -= (someDamage + 2.0)
                patience -= someDamage

                // Check if critical activation
                if (patience <= 0) {
                    messageQuote = whoActivationQuote
                    activateBoss(someDamager)
                }
            }
            // Send Messages to all
            for (somePlayer in someDamager.world.players) {
                somePlayer.sendMessage(messageQuote)
            }
        }
        else {
            if (someDamager is Player) {
                attackToDamager(someDamager)
            }
        }
    }


    // Appeasement Mechanic
    fun appeasementCheck(somePlayer: Player, someItem: Item) {
        // Check if players in gift cooldown map
        if (!playersGiftCooldown.containsKey(somePlayer.uniqueId)) {
            playersGiftCooldown[somePlayer.uniqueId] = System.currentTimeMillis()
            calculateGiftTable(somePlayer, someItem)
        }
        else {
            val timeElapsed: Long = System.currentTimeMillis() - playersGiftCooldown[somePlayer.uniqueId]!!
            // Gift Cooldown
            if (timeElapsed >= 5000) {
                playersGiftCooldown[somePlayer.uniqueId] = System.currentTimeMillis()
                calculateGiftTable(somePlayer, someItem)
            }
        }
    }

    private fun calculateGiftTable(givingPlayer: Player, giftedItem: Item) {
        val giftedMaterial: Material = giftedItem.itemStack.type
        // Check if item in table
        if (giftedMaterial in itemLootTable) {

            // SEPERATE INTO LIKE AND DISLIKE TABLES!!!
            when (giftedMaterial) {
                Material.NETHERITE_INGOT -> {
                    bossEntity?.world!!.spawnParticle(Particle.SPELL_WITCH, givingPlayer.location, 15, 1.0, 1.0, 1.0)
                    appeasement += 5
                    if (appeasement > 50) {
                        givingPlayer.inventory.addItem(giftLootTable["ArtificialStarUnit"]!!.createItemStack(1, null , null))
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Hmm casted Neutronium Bark... Here take this special Unit-${ChatColor.MAGIC}092412X.")
                    }
                    else {
                        givingPlayer.inventory.addItem(giftLootTable["ArtificialStarUnit"]!!.createItemStack(1, null , null))
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Hmm casted Neutronium Bark... Here are some quantum-entangled vacuums repurposed as storage " +
                                "that might help you as well as some gifts")
                    }
                }
                Material.DIAMOND, Material.EMERALD, Material.AMETHYST_SHARD -> {
                    appeasement -= 1
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}These gems are not that refined according to standards. But... here is something for such work.")
                }
                Material.COPPER_INGOT, Material.IRON_INGOT, Material.GOLD_INGOT -> {
                    appeasement += 1
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}The tribute of raw materials express the loyalty and growth of this world...")
                }
                Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.COPPER_BLOCK -> {
                    appeasement += 4
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}This much raw materials should garner good attention...")
                    if (appeasement > 50) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}This will help you towards industrialization...")
                    }
                }
                Material.ACACIA_LOG, Material.OAK_LOG, Material.SPRUCE_LOG, Material.DARK_OAK_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.MANGROVE_LOG,
                Material.ACACIA_WOOD, Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.DARK_OAK_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD, Material.MANGROVE_WOOD -> {
                    appeasement += 2
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Wood is always a commodity that should be accepted!")
                    if (appeasement > 50) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}This will help you towards industrialization...")
                    }
                }
                Material.ROTTEN_FLESH -> {
                    appeasement -= 2
                    patience -= 2
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}YOu think of me so low and stupid. I believe this world does not deserve anything close to respect...")
                }
                Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT -> {
                    appeasement -= 2
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}I can not accept something that crude...")
                }
                Material.ENCHANTED_BOOK ->  {
                    appeasement += 4
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Enchanted Literature! Something quite interesting this test-site was made for...and a Good Read")
                    if (appeasement < 35) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}I am pleased so far.. here have some extra gifts...")
                    }
                    else if (appeasement >= 35) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Tokens for your prosperity and of the upcoming ${ChatColor.MAGIC}World Integration Procedure")
                    }
                }
                Material.BOOK -> {
                    appeasement += 1
                    if (appeasement < 45) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Hopefully this culture is not so dull as other test-s... Nevermind...")
                    }
                    else {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}So far I can say I am enjoying this...")
                    }
                }
                Material.ENDER_CHEST, Material.SHULKER_BOX -> {
                    appeasement += 3
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Sub-Dimensional Storage. A step towards industrialization I see...And excellent presents!")
                }
                Material.ENCHANTED_GOLDEN_APPLE -> {
                    appeasement += 10
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}How did you get this?... I did not hear that Vail planted Aether roots from Lupercal...")
                    givingPlayer.sendMessage("${ChatColor.DARK_GRAY}${ChatColor.ITALIC}Do not alert the others... Here take this. Keep it safe, it will help you soon to come...")
                }
                Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.ORANGE_TULIP,
                Material.RED_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.SUNFLOWER,
                Material.LILAC, Material.ROSE_BUSH, Material.PEONY -> {
                    appeasement += 1
                    if (appeasement < 45) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}For an uncivilized world, flowers still bloom... Hopefully you do as well...")
                    }
                    else if (appeasement >= 45) {
                        givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}This should grow your knowledge and strength in time...")
                    }
                }
                else -> {
                    givingPlayer.sendMessage("${ChatColor.LIGHT_PURPLE}[The Ambassador] ${ChatColor.RESET}Well...No.")
                }
            }
            // Make thread to delete
            giftedItem.remove()
        }
    }
}

